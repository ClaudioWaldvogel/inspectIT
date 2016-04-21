package rocks.inspectit.server.diagnosis.engine.session.result;

import rocks.inspectit.server.diagnosis.engine.session.SessionContext;

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
