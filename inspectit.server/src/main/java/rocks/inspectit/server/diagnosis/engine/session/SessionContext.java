package rocks.inspectit.server.diagnosis.engine.session;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.execution.store.IRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.util.SessionVariables;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class SessionContext<I> {

	private I input;
	private final Set<RuleDefinition> backupRules;
	private Set<RuleDefinition> ruleSet;
	private IRuleOutputStorage storage;
	private SessionVariables sessionVariables;

	public SessionContext(Set<RuleDefinition> rules, IRuleOutputStorage storage) {
		// Protected the initial rules from being manipulated
		this.backupRules = ImmutableSet.copyOf(rules);
		this.ruleSet = new HashSet<>();
		this.storage = storage;
	}

	// -------------------------------------------------------------
	// Methods: LifeCycle
	// -------------------------------------------------------------

	public SessionContext<I> activate(I input, SessionVariables variables) {
		this.input = input;
		// ensure a shallow copy, we must never ever operate on the original RuleSet
		this.ruleSet.addAll(backupRules);
		this.sessionVariables = variables;
		return this;
	}

	public SessionContext<I> passivate() {
		this.input = null;
		this.ruleSet.clear();
		this.storage.clear();
		this.sessionVariables = null;
		return this;
	}

	public SessionContext<I> destroy() {
		this.input = null;
		this.storage = null;
		this.ruleSet = null;
		this.sessionVariables = null;
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
	 * Gets {@link #sessionVariables}.
	 *
	 * @return {@link #sessionVariables}
	 */
	public SessionVariables getSessionVariables() {
		return sessionVariables;
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
