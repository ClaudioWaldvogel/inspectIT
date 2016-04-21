package rocks.inspectit.server.diagnosis.engine.rule.execution;

import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinition;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class ExecutionContext {

    private Object instance;
    private RuleInput input;
    private RuleDefinition definition;

    public ExecutionContext(RuleDefinition definition,
                            Object instance,
                            RuleInput input) {
        this.definition = definition;
        this.instance = instance;
        this.input = input;
        
    }

    //-------------------------------------------------------------
    // Methods: Accessors
    //-------------------------------------------------------------

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
}
