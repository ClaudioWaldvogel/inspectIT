package rocks.inspectit.server.diagnosis.engine.rule.execution;

import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinition;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class RuleExecutionException extends RuntimeException {

    public RuleExecutionException(String message, ExecutionContext context) {
        this(message, context, null);
    }

    public RuleExecutionException(String message, ExecutionContext context, Throwable cause) {
        super(prefix(message, context.getDefinition()), cause);
    }

    private static String prefix(String message, RuleDefinition definition) {
        return "Rule: (" + definition.getName() + ") failed with error: " + message;
    }

}
