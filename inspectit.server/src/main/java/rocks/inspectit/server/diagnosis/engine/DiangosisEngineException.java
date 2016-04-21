package rocks.inspectit.server.diagnosis.engine;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class DiangosisEngineException extends RuntimeException {

	private static final long serialVersionUID = 8461985903909289169L;

	public DiangosisEngineException(String message) {
		super(message);
	}

	public DiangosisEngineException(String message, Throwable cause) {
		super(message, cause);
	}
}
