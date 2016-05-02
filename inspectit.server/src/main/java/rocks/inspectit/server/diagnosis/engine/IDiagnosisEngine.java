/**
 *
 */
package rocks.inspectit.server.diagnosis.engine;

import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;

/**
 * @author Claudio Waldvogel
 *
 * @param <I>
 */
public interface IDiagnosisEngine<I> {

	void analyze(I input);

	void analyze(I input, SessionVariables variables);

	void shutdown(boolean awaitShutdown) throws Exception;

}