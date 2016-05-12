package rocks.inspectit.server.diagnosis.engine.session.exception;

/**
 * Common exception for all failures which can occur while executing a <code>Session</code>.
 *
 * @author Claudio Waldvogel
 */
public class SessionException extends RuntimeException {

    /**
     * Default Constructor
     *
     * @param message The error message
     */
    public SessionException(String message) {
        super(message);
    }

    /**
     * Constructor with cause
     *
     * @param message The error message
     * @param cause   The cause of the error
     */
    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
