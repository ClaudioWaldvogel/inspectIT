package rocks.inspectit.server.diagnosis.engine.session;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public interface ISessionResultHandler<R> {

    void handle(R result);
}
