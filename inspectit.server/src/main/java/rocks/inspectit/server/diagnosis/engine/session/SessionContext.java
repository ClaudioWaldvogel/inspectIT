package rocks.inspectit.server.diagnosis.engine.session;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.execution.store.IRuleOutputStorage;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class SessionContext<I> {

	private I input;
	private final Set<RuleDefinition> backupRules;
	private Set<RuleDefinition> ruleSet;
	private IRuleOutputStorage storage;

	public SessionContext(Set<RuleDefinition> rules, IRuleOutputStorage storage) {
		// Protected the initial rules from being manipulated
		this.backupRules = ImmutableSet.copyOf(rules);
		this.ruleSet = new HashSet<>();
		this.storage = storage;
	}

	// -------------------------------------------------------------
	// Methods: LifeCycle
	// -------------------------------------------------------------

	public SessionContext<I> activate(I input) {
		this.input = input;
		// ensure a shallow copy, we must never ever operate on the original RuleSet
		this.ruleSet.addAll(backupRules);
		return this;
	}

	public SessionContext<I> passivate() {
		this.input = null;
		this.ruleSet.clear();
		this.storage.clear();
		return this;
	}

	public SessionContext<I> destroy() {
		this.input = null;
		this.storage = null;
		this.ruleSet = null;
		return this;
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #input}.
	 *
	 * @return {@link #input}
	 */
	public I getInput() {
		return input;
	}

	/**
	 * Gets {@link #ruleSet}.
	 *
	 * @return {@link #ruleSet}
	 */
	public Set<RuleDefinition> getRuleSet() {
		return ruleSet;
	}

	/**
	 * Gets {@link #storage}.
	 *
	 * @return {@link #storage}
	 */
	public IRuleOutputStorage getStorage() {
		return storage;
	}
}
