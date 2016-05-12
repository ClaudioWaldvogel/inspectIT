package rocks.inspectit.server.diagnosis.engine.session;

/**
 * A <code>ISessionResultCollector</code> transforms a <code>SessionContext</code> to a proper result object. The <code>ISessionResultCollector</code> is executed as soon as a <code>Session</code>
 * completed it's work and is ready to provide the results.
 *
 * @param <I>
 * 		The type of input the <code>Session</code> processed
 * @param <R>
 * 		The type of result this <code>ISessionResultCollector</code> produces
 * @author Claudio Waldvogel
 */
public interface ISessionResultCollector<I, R> {

	/**
	 * Collects all results from <code>SessionContext</code>. It is intended to collect anything and produce anything.
	 * <p>
	 * <strong>Be aware that one must no hold a reference to the SessionContext!!. SessionContext will be destroyed as soon as results are collected. Thus, values will be unavailable and
	 * lost!</strong>
	 *
	 * @param sessionContext
	 * 		The SessionContext
	 * @return A new result of type R
	 */
	R collect(SessionContext<I> sessionContext);
}
