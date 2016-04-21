package rocks.inspectit.server.diagnosis.engine.session;

import org.apache.commons.pool.BasePoolableObjectFactory;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineConfiguration;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class SessionFactory<I, R> extends BasePoolableObjectFactory<Session<I, R>> {

	private final DiagnosisEngineConfiguration<I, R> configuration;

	public SessionFactory(DiagnosisEngineConfiguration<I, R> configuration) {
		this.configuration = configuration;
	}

	@Override
	public Session<I, R> makeObject() throws Exception {
		return Session.<I, R> builder().setNumRuleWorkers(configuration.getNumRuleWorkers()).setStorageClass(configuration.getStorageClass()).setRuleDefinitions(configuration.getRuleDefinitions())
				.setSessionResultCollector(configuration.getResultCollector()).build();
	}

	@Override
	public void passivateObject(Session<I, R> session) throws Exception {
		session.passivate();
	}

	@Override
	public void destroyObject(Session<I, R> session) throws Exception {
		session.destroy();
	}
}
