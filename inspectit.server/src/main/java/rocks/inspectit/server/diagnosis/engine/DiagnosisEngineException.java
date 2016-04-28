package rocks.inspectit.server.diagnosis.engine;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class DiagnosisEngineException extends RuntimeException {

	private static final long serialVersionUID = 8461985903909289169L;

	public DiagnosisEngineException(String message) {
		super(message);
	}

	public DiagnosisEngineException(String message, Throwable cause) {
		super(message, cause);
	}
}
