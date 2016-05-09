package rocks.inspectit.server.diagnosis.engine.session;

/**
 * A callback for handling the results or errors of a {@link Session} execution.
 *
 * @param <R>
 * 		The type of result to be handled
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public interface ISessionCallback<R> {

	/**
	 * Invoked with the result of a <code>Session</code> execution when it executes successful. The type of the result depends in the used {@link ISessionResultCollector}.
	 *
	 * @param result
	 * 		The result of a <code>Session</code> execution
	 * @see ISessionResultCollector
	 */
	void onSuccess(R result);

	/**
	 * Invoked when the <code>Session</code> execution failed.
	 *
	 * @param throwable
	 * 		The cause why the <code>Session</code> failed
	 */
	void onFailure(Throwable throwable);
}
