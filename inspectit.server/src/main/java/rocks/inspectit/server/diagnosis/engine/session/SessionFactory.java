package rocks.inspectit.server.diagnosis.engine.session;

import org.apache.commons.pool.BasePoolableObjectFactory;
import rocks.inspectit.server.diagnosis.engine.DiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineConfiguration;

/**
 * BasePoolableObjectFactory implementation to create poolable {@link Session} instances. SessionFactory is used by {@link SessionPool}.
 *
 * @author Claudio Waldvogel
 * @see BasePoolableObjectFactory
 * @see SessionPool
 */
public class SessionFactory<I, R> extends BasePoolableObjectFactory<Session<I, R>> {

	/**
	 * The top-level {@link DiagnosisEngineConfiguration} configuration. Ths configuration might be used to configuration the factory.
	 *
	 * @see DiagnosisEngineConfiguration
	 * @see DiagnosisEngine
	 */
	private final DiagnosisEngineConfiguration<I, R> configuration;

	/**
	 * Default constructor to create new {@link SessionFactory}s
	 *
	 * @param configuration
	 * 		The {@link DiagnosisEngineConfiguration}
	 */
	public SessionFactory(DiagnosisEngineConfiguration<I, R> configuration) {
		this.configuration = configuration;
	}

	//-------------------------------------------------------------
	// Interface Implementation: BasePoolableObjectFactory
	//-------------------------------------------------------------

	@Override
	public Session<I, R> makeObject() throws Exception {
		//Utilize Session#Builder to create a new Session. Session is configured with values from the the DiagnosisEngineConfiguration.
		return Session.<I, R>builder().setNumRuleWorkers(configuration.getNumRuleWorkers()).setStorageClass(configuration.getStorageClass()).setRuleDefinitions(configuration.getRuleDefinitions())
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
