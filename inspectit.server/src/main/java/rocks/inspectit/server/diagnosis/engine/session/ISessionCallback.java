package rocks.inspectit.server.diagnosis.engine.session;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public interface ISessionCallback<R> {

	void onSuccess(R result);

	void onFailure(Throwable t);
}
