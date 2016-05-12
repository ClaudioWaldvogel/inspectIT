package rocks.inspectit.server.diagnosis.engine.session;

import com.google.common.collect.ImmutableSet;
import rocks.inspectit.server.diagnosis.engine.DiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.store.IRuleOutputStorage;

import java.util.HashSet;
import java.util.Set;

/**
 * A <code>SessionContext</code> represents the runtime information of a currently processing {@link Session}. Each {@link SessionContext} is associated to exactly one {@link Session} and is not
 * viable without a <code>Session</code>. <br> <code>SessionContext</code>s implement the same life cycle as <code>Sessions</code>: {@link #activate(Object, SessionVariables)}, {@link #passivate()},
 * {@link #destroy()}.
 *
 * @param <I> The type of the input of this session.s
 * @author Claudio Waldvogel
 * @see Session
 */
public class SessionContext<I> {

    /**
     * The input object the session is analyzing
     */
    private I input;

    /**
     * ImmutableSet of all available {@link RuleDefinition}s within the {@link DiagnosisEngine}. This is a backup to restore the {@link #ruleSet} to an initial state as soon as the
     * <code>SessionContext</code> gets activated.
     */
    private final ImmutableSet<RuleDefinition> backupRules;

    /**
     * The set of processable {@link RuleDefinition}s. As soon as a Session executed a <code>RuleDefinition</code>, this definition is evicted from the set. Thus it is easily possible to determine
     * when a <code>Session</code> executed all possible <code>RuleDefinition</code>s.
     *
     * @see RuleDefinition
     * @see Session
     */
    private Set<RuleDefinition> ruleSet;

    /**
     * The <code>IRuleOutputStorage</code> used in this <code>Session</code>.
     *
     * @see IRuleOutputStorage
     */
    private IRuleOutputStorage storage;

    /**
     * The available <code>SessionVariables</code> for this <code>Session</code> execution
     */
    private SessionVariables sessionVariables;

    /**
     * Default constructor to create new <code>SessionContext</code>s.
     *
     * @param rules   The set of <code>RuleDefinition</code>s
     * @param storage The <code>IRuleOutputStorage</code> implementation
     * @see RuleDefinition
     * @see IRuleOutputStorage
     */
    SessionContext(Set<RuleDefinition> rules, IRuleOutputStorage storage) {
        // Protected the initial rules from being manipulated
        this.backupRules = ImmutableSet.copyOf(rules);
        this.ruleSet = new HashSet<>();
        this.storage = storage;
    }

    // -------------------------------------------------------------
    // Methods: LifeCycle
    // -------------------------------------------------------------

    /**
     * Activate the <code>SessionContext</code>. The {@link #ruleSet} is restored to {@link #backupRules}.
     *
     * @param input     The object to to be analyzed.
     * @param variables The valid <code>SessionVariables</code>,
     * @return The SessionContext itself.
     * @see Session
     * @see Session#activate(Object, SessionVariables)
     * @see SessionVariables
     */
    SessionContext<I> activate(I input, SessionVariables variables) {
        this.input = input;
        // ensure a shallow copy, we must never ever operate on the original RuleSet
        this.ruleSet.addAll(backupRules);
        this.sessionVariables = variables;
        return this;
    }

    /**
     * Passivates the <code>SessionContext</code> by clearing th {@link #ruleSet}, the {@link #sessionVariables},  the {@link #storage}, and the  {@link #input}.
     *
     * @return The SessionContext itself.
     * @see Session
     * @see Session#passivate()
     */
    SessionContext<I> passivate() {
        this.input = null;
        this.ruleSet.clear();
        this.storage.clear();
        this.sessionVariables = null;
        return this;
    }

    /**
     * Passivates the <code>SessionContext</code> by nulling th {@link #ruleSet}, the {@link #sessionVariables},  the {@link #storage}, and the  {@link #input}.
     *
     * @return The SessionContext itself.
     * @see Session
     * @see Session#destroy()
     */
    SessionContext<I> destroy() {
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
