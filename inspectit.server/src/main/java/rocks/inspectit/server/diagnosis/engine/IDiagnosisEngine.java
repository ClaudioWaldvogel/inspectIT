/**
 *
 */
package rocks.inspectit.server.diagnosis.engine;

import org.springframework.stereotype.Component;

import rocks.inspectit.server.diagnosis.engine.util.SessionVariables;

/**
 * @author Claudio Waldvogel
 *
 * @param <I>
 * @param <R>
 */
@Component
public interface IDiagnosisEngine<I, R> {

	void analyze(I input) throws Exception;

	void analyze(I input, SessionVariables variables);

	void shutdown(boolean awaitShutdown) throws Exception;

}