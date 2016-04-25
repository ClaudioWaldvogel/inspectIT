package rocks.inspectit.server.diagnosis.engine.session;

import static com.google.common.base.Preconditions.checkNotNull;
import static rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.tryInstantiate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import rocks.inspectit.server.diagnosis.engine.rule.Rules;
import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.rule.execution.store.IRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.result.ISessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.engine.util.SessionVariables;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class Session<I, R> implements Callable<R> {

	private static final Logger log = LoggerFactory.getLogger(Session.class);
	private State state = State.NEW;
	private SessionContext<I> sessionContext;
	private ExecutorService executor;
	private ISessionResultCollector<I, R> resultCollector;
	private int shutDownTimeout;

	// -------------------------------------------------------------
	// Methods: Construction
	// -------------------------------------------------------------

	/**
	 * Private Constructor, Session is always instantiated from the Builder
	 */
	private Session() {
	}

	public static <I, R> Builder<I, R> builder() {
		return new Builder<>();
	}

	public static class Builder<I, R> {
		private int numRuleWorkers = 1;
		private int shutDownTimeout = 2;
		private Set<RuleDefinition> ruleDefinitions;
		private Class<? extends IRuleOutputStorage> storageClass;
		private ISessionResultCollector<I, R> collector;

		public Builder() {
		}

		public Builder<I, R> setNumRuleWorkers(int workers) {
			this.numRuleWorkers = workers;
			return this;
		}

		public Builder<I, R> setStorageClass(Class<? extends IRuleOutputStorage> storageClass) {
			this.storageClass = storageClass;
			return this;
		}

		public Builder<I, R> setShutDownTimeout(int shutDownTimeout) {
			this.shutDownTimeout = shutDownTimeout;
			return this;
		}

		public Builder<I, R> setRuleDefinitions(Set<RuleDefinition> ruleDefinitions) {
			this.ruleDefinitions = ruleDefinitions;
			return this;
		}

		public Builder<I, R> setSessionResultCollector(ISessionResultCollector<I, R> collector) {
			this.collector = collector;
			return this;
		}

		public Session<I, R> build() {
			// sanity checks
			checkNotNull(ruleDefinitions);
			checkNotNull(storageClass);
			checkNotNull(collector);
			numRuleWorkers = numRuleWorkers > 0 ? numRuleWorkers : 1;
			shutDownTimeout = shutDownTimeout >= 2 ? shutDownTimeout : 2;

			// Create a new Session
			Session<I, R> session = new Session<>();
			session.executor = Executors.newFixedThreadPool(numRuleWorkers);
			session.shutDownTimeout = shutDownTimeout;
			session.sessionContext = new SessionContext<>(ruleDefinitions, tryInstantiate(storageClass));
			session.resultCollector = collector;
			return session;
		}
	}

	// -------------------------------------------------------------
	// Interface Implementation: Callable
	// -------------------------------------------------------------

	@Override
	public R call() throws Exception {
		return process().collectResults();
	}

	public R collectResults() {
		return resultCollector.collect(sessionContext);
	}

	// -------------------------------------------------------------
	// Methods: LifeCycle -> reflects the life cycle of a
	// org.apache.commons.pool2.PooledObject
	// -------------------------------------------------------------

	public Session<I, R> activate(I input) {
		return activate(input, new SessionVariables());
	}

	public Session<I, R> activate(I input, SessionVariables variables) {
		switch (state) {
		case NEW:
		case PASSIVATED:
			// All we need to do is to reactivate the SessionContext
			sessionContext.activate(input, variables);
			state = State.ACTIVATED;
			break;
		case DESTROYED:
			throw new IllegalStateException("Session already destroyed.");
		default:
			throw new IllegalStateException("Session can not enter ACTIVATED stated from: " + state + "state. Ensure Session is in NEW or PASSIVATED state when activating.");
		}
		return this;
	}

	public Session<I, R> process() {
		switch (state) {
		case ACTIVATED:
			sessionContext.getStorage().insert(Rules.triggerRuleOutput(sessionContext.getInput()));
			doProcess();
			state = State.PROCESSED;
			break;
		default:
			throw new IllegalStateException("Session can not enter process stated from: " + state + "state. Ensure that Session is in ACTIVATED state before processing.");
		}
		return this;
	}

	public Session<I, R> passivate() {
		switch (state) {
		case PROCESSED:
			sessionContext.passivate();
			state = State.PASSIVATED;
			break;
		case ACTIVATED:
			log.warn("Session gets passivated but was not yet processed.");
			break;
		case DESTROYED:
			log.warn("Session gets passivated but was already destroyed.");
			break;
		default:
			throw new IllegalStateException("Session can not enter PASSIVATED stated from: " + state + "state. Ensure that Session is in PROCESSED state before it gets passivated.");
		}
		return this;
	}

	public Session<I, R> destroy() {
		switch (state) {
		case PROCESSED:
			// We can destroy the session but it was not yet passivated. To stay in sync with the
			// state lifeCycle
			// we passivate first
			passivate();
			destroy();
			break;
		case DESTROYED:
			// Already destroyed. Warn?
			break;
		case NEW:
		case ACTIVATED:
			log.warn("Session is destroy before it was processed.");
		default:
			try {
				executor.shutdown();
				if (!executor.awaitTermination(shutDownTimeout, TimeUnit.SECONDS)) {
					log.error("Session Executor did not shut down within: {} seconds.", shutDownTimeout);
				}
			} catch (InterruptedException e) {
				throw new SessionException("Failed to destroy Session", e);
			} finally {
				// ensure context is properly destroyed and all collected data is wiped out
				sessionContext.destroy();
				// aggressively null out
				executor = null;
				sessionContext = null;
			}
		}
		return this;
	}

	// -------------------------------------------------------------
	// Methods: Internals
	// -------------------------------------------------------------

	private void doProcess() {
		Set<Execution> nextRules = findNextRules();
		while (nextRules.size() > 0) {
			try {
				List<Future<Collection<RuleOutput>>> futures = executor.invokeAll(nextRules);
				for (Future<Collection<RuleOutput>> future : futures) {
					try {
						// insert latest results in storage.
						// The insertion waits till the future returns
						sessionContext.getStorage().insert(future.get());
					} catch (ExecutionException ex) {
						throw new SessionException("Failed to retrieve RuleOutput", ex);
					}
				}
			} catch (InterruptedException ex) {
				throw new SessionException("Failed to retrieve RuleOutput", ex);
			}
			nextRules = findNextRules();
		}
	}

	private Set<Execution> findNextRules() {
		Set<String> available = sessionContext.getStorage().availableTagTypes();
		Set<Execution> nextRules = new HashSet<>();
		Iterator<RuleDefinition> iterator = sessionContext.getRuleSet().iterator();
		while (iterator.hasNext()) {
			RuleDefinition rule = iterator.next();
			if (rule.getFireCondition().canFire(available)) {
				// wrap RuleDefinition in a callable object to be used with ExecutorService
				nextRules.add(new Execution(rule));
				iterator.remove();
			}
		}
		return nextRules;
	}

	private Collection<RuleInput> collectInputs(RuleDefinition definition) {
		Set<String> requiredInputTags = definition.getFireCondition().getTagTypes();
		Collection<RuleOutput> leafOutputs = sessionContext.getStorage().findLeafsByTags(requiredInputTags);
		Set<RuleInput> inputs = Sets.newHashSet();
		// A single can produce n inputs. Each embedded tag in ruleOutput.getTags() will be
		// reflected in a new RuleInput
		// Although this is an O(n²) loop the iterated lists are expected to be rather short.
		// Also the nested while loop is expected to be very short. (Probably in )
		for (RuleOutput output : leafOutputs) {
			for (Tag leafTag : output.getTags()) {
				Collection<Tag> tags = Tags.unwrap(leafTag, requiredInputTags);
				if (tags.size() != requiredInputTags.size()) {
					log.warn("Invalid Value definitions for {}. All values must be reachable from the latest Tag " + "value.", definition.getName());
				} else {
					// Create and store a new RuleInput
					inputs.add(new RuleInput(leafTag, tags));
				}
			}
		}
		return inputs;
	}

	// -------------------------------------------------------------
	// Inner classes
	// -------------------------------------------------------------

	private class Execution implements Callable<Collection<RuleOutput>> {

		private final RuleDefinition definition;

		public Execution(RuleDefinition definition) {
			this.definition = definition;
		}

		@Override
		public Collection<RuleOutput> call() throws Exception {
			return definition.execute(collectInputs(definition), Session.this.sessionContext.getSessionVariables());
		}
	}

	private enum State {
		/**
		 * The initial State of each Session.
		 */
		NEW,

		/**
		 * The state as soon as an <code>Session</code> gets activated. This stated can be entered
		 * from <code>NEW</code> and <code>PASSIVATED</code> states.
		 */
		ACTIVATED,

		/**
		 * An <code>Session</code> enters the <code>PROCESSING</code> state after all applicable
		 * rules were executed.
		 */
		PROCESSED,
		/**
		 * An <code>Session</code> can enter the <code>PASSIVATED</code> stated only from
		 * <code>PROCESSED</code> stated. <code>PASSIVATED</code> is the only state which enables a
		 * transition back to <code>ACTIVATED</code>.
		 */
		PASSIVATED,

		/**
		 * <code>Session</code> is destroyed and not longer usable
		 */
		DESTROYED
	}

}
