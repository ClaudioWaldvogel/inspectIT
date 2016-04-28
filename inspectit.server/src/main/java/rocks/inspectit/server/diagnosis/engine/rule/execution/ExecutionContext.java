package rocks.inspectit.server.diagnosis.engine.rule.execution;

import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.util.SessionVariables;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class ExecutionContext {

    private Object instance;
    private RuleInput input;
    private RuleDefinition definition;
    private SessionVariables sessionParameters;

    public ExecutionContext(RuleDefinition definition, Object instance, RuleInput input) {
        this(definition, instance, input, new SessionVariables());
    }

    public ExecutionContext(RuleDefinition definition, Object instance, RuleInput input, SessionVariables sessionParameters) {
        this.definition = definition;
        this.instance = instance;
        this.input = input;
        this.sessionParameters = sessionParameters;

    }

    // -------------------------------------------------------------
    // Methods: Accessors
    // -------------------------------------------------------------

    /**
     * Gets {@link #instance}.
     *
     * @return {@link #instance}
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * Gets {@link #input}.
     *
     * @return {@link #input}
     */
    public RuleInput getRuleInput() {
        return input;
    }

    /**
     * Gets {@link #definition}.
     *
     * @return {@link #definition}
     */
    public RuleDefinition getDefinition() {
        return definition;
    }

    /**
     * Gets {@link #sessionParameters}.
     *
     * @return {@link #sessionParameters}
     */
    public SessionVariables getSessionParameters() {
        return sessionParameters;
    }

}
