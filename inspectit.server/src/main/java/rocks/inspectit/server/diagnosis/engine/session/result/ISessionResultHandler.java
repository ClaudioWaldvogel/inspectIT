package rocks.inspectit.server.diagnosis.engine.session.result;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public interface ISessionResultHandler<R> {

    void handle(R result);
}
