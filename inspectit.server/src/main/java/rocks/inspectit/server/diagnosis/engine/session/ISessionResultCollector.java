package rocks.inspectit.server.diagnosis.engine.session;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public interface ISessionResultCollector<I, R> {

	/**
	 * <strong>SessionContext</strong> will be destroyed!!
	 *
	 * @param sessionContext
	 * @return
	 */
	R collect(SessionContext<I> sessionContext);
}
