package rocks.inspectit.server.diagnosis.engine.rule.definition;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class RuleDefinitionException extends RuntimeException{

    public RuleDefinitionException(String message) {
        super(message);
    }

    public RuleDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
