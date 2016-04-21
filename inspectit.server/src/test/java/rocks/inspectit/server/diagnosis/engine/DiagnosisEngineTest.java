/**
 *
 */
package rocks.inspectit.server.diagnosis.engine;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.DummyRules;
import rocks.inspectit.server.diagnosis.engine.rule.execution.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.result.DefaultSessionResult;
import rocks.inspectit.server.diagnosis.engine.session.result.DefaultSessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.result.ISessionResultHandler;

/**
 * @author Claudio Waldvogel
 *
 */
public class DiagnosisEngineTest {

	@Test
	public void testEngine() {

		final List<DefaultSessionResult<String>> results = new ArrayList<>();

		DiagnosisEngineConfiguration<String, DefaultSessionResult<String>> configuration = new DiagnosisEngineConfiguration<String, DefaultSessionResult<String>>().setNumSessionWorkers(2)
				.setRuleClasses(DummyRules.classes()).setStorageClass(DefaultRuleOutputStorage.class).setResultCollector(new DefaultSessionResultCollector<String>())
				.setResultHandler(new ISessionResultHandler<DefaultSessionResult<String>>() {
					@Override
					public void handle(DefaultSessionResult<String> result) {
						results.add(result);
					}
				});

		DiagnosisEngine<String, DefaultSessionResult<String>> diagnosisEngine = new DiagnosisEngine<>(configuration);

		try {
			diagnosisEngine.analyze("Trace");
			diagnosisEngine.analyze("Trace2");
			diagnosisEngine.shutdown(true);
			Assert.assertEquals(results.size(), 2);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
