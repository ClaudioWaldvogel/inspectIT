/**
 *
 */
package rocks.inspectit.server.diagnosis.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineConfiguration;
import rocks.inspectit.server.diagnosis.engine.IDiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.rule.api.Action;
import rocks.inspectit.server.diagnosis.engine.rule.api.InjectionType;
import rocks.inspectit.server.diagnosis.engine.rule.api.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.api.TagValue;
import rocks.inspectit.server.diagnosis.engine.rule.execution.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.result.DefaultSessionResult;
import rocks.inspectit.server.diagnosis.engine.session.result.DefaultSessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.result.ISessionResultHandler;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * @author Claudio Waldvogel
 *
 */
public class DiagnosisServiceImpl implements IDiagnosisService {

	private List<DefaultSessionResult<InvocationSequenceData>> results;

	private IDiagnosisEngine<InvocationSequenceData, DefaultSessionResult<InvocationSequenceData>> engine;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(DiagnosisEvent diagnosisEvent) {
		if (diagnosisEvent instanceof DiagnosisEvent) {
			try {
				engine.analyze((InvocationSequenceData) diagnosisEvent.getSource());
			} catch (Exception e) {
				// TODO
				e.printStackTrace();
			}
		}

	}

	@PostConstruct
	public void init() {
		results = new ArrayList<>();

		DiagnosisEngineConfiguration<InvocationSequenceData, DefaultSessionResult<InvocationSequenceData>> configuration = new DiagnosisEngineConfiguration<InvocationSequenceData, DefaultSessionResult<InvocationSequenceData>>()
				.setNumSessionWorkers(2).setRuleClass(ExtractLoggingData.class).setRuleClass(InspectLogginData.class).setStorageClass(DefaultRuleOutputStorage.class)
				.setResultCollector(new DefaultSessionResultCollector<InvocationSequenceData>()).setResultHandler(new DiagnosisResultHandler());

		engine = new DiagnosisEngine<>(configuration);

	}

	private class DiagnosisResultHandler implements ISessionResultHandler<DefaultSessionResult<InvocationSequenceData>> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handle(DefaultSessionResult<InvocationSequenceData> result) {
			DiagnosisServiceImpl.this.results.add(result);
		}
	}

	@Rule(name = "ExtractLoggingData")
	public static class ExtractLoggingData {

		@TagValue(tagType = Tags.ROOT_TAG)
		private InvocationSequenceData is;

		@Action(resultTag = "LogginData")
		public TimerData execute() {
			return is.getTimerData();
		}

	}

	@Rule(name = "InspectLogginData")
	public static class InspectLogginData {

		@TagValue(tagType = "LogginData", injectionType = InjectionType.BY_TAG)
		private Tag timeTag;

		@Action(resultTag = "InspectedLogginData")
		public double execute() {
			return ((TimerData) timeTag.getValue()).getCpuDuration();
		}

	}

}
