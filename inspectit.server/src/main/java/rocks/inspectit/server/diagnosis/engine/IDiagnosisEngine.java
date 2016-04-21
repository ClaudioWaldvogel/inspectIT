/**
 *
 */
package rocks.inspectit.server.diagnosis.engine;

import org.springframework.stereotype.Component;

/**
 * @author Claudio Waldvogel
 *
 * @param <I>
 * @param <R>
 */
@Component
public interface IDiagnosisEngine<I, R> {

	void analyze(I input) throws Exception;

	void shutdown(boolean awaitShutdown) throws Exception;

}