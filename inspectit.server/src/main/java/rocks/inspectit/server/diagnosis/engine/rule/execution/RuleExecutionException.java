package rocks.inspectit.server.diagnosis.engine.rule.execution;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class RuleExecutionException extends RuntimeException {

    public RuleExecutionException(String message) {
        super(message);
    }

    public RuleExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
