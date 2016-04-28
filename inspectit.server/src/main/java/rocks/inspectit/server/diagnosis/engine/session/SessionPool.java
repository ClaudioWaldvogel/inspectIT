package rocks.inspectit.server.diagnosis.engine.session;

import org.apache.commons.pool.impl.GenericObjectPool;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineConfiguration;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class SessionPool<I, R> extends GenericObjectPool<Session<I, R>> {

	public SessionPool(DiagnosisEngineConfiguration<I, R> engineConfiguration) {
		this(engineConfiguration, null);
	}

	public SessionPool(DiagnosisEngineConfiguration<I, R> engineConfiguration, GenericObjectPool.Config poolConfiguration) {
		super(new SessionFactory<>(engineConfiguration));

		if (poolConfiguration != null) {
			setConfig(poolConfiguration);
		} else {
			GenericObjectPool.Config config = new GenericObjectPool.Config();
			config.whenExhaustedAction = GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION;
			config.maxActive = engineConfiguration.getNumSessionWorkers();
			setConfig(config);
		}
	}

	/**
	 * Only change to borrow object and activate with an input object
	 *
	 * @param input
	 * @return
	 */
	public Session<I, R> borrowObject(I input, SessionVariables variables) {
		Session<I, R> session;
		try {
			session = super.borrowObject();
			session.activate(input, variables);
		} catch (Exception e) {
			throw new RuntimeException("Failed to borrow object from SessionPool.", e);
		}
		return session;
	}

}
