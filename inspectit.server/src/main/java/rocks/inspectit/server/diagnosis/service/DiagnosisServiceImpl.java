/**
 *
 */
package rocks.inspectit.server.diagnosis.service;

import rocks.inspectit.server.diagnosis.engine.IDiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.session.DefaultSessionResult;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author Claudio Waldvogel
 *
 */
public class DiagnosisServiceImpl implements IDiagnosisService {

	private List<DefaultSessionResult<InvocationSequenceData>> results;

	private IDiagnosisEngine<InvocationSequenceData> engine;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(DiagnosisEvent diagnosisEvent) {
		if (diagnosisEvent != null) {
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
		/*results = new ArrayList<>();

		DiagnosisEngineConfiguration<InvocationSequenceData, DefaultSessionResult<InvocationSequenceData>> configuration = new DiagnosisEngineConfiguration<InvocationSequenceData, DefaultSessionResult<InvocationSequenceData>>()
				.setNumSessionWorkers(2).setRuleClass(ExtractLoggingData.class).setRuleClass(InspectLogginData.class).setStorageClass(DefaultRuleOutputStorage.class)
				.setResultCollector(new DefaultSessionResultCollector<InvocationSequenceData>()).setSessionCallback(new DiagnosisResultHandler());

		engine = new DiagnosisEngine<>(configuration);*/

	}


	@Rule(name = "ExtractLoggingData")
	public static class ExtractLoggingData {

		@TagValue(type = Tags.ROOT_TAG)
		private InvocationSequenceData is;

		@Action(resultTag = "LogginData")
		public TimerData execute() {
			return is.getTimerData();
		}

	}

	@Rule(name = "InspectLogginData")
	public static class InspectLogginData {

		@TagValue(type = "LogginData", injectionStrategy = TagValue.InjectionStrategy.BY_TAG)
		private Tag timeTag;

		@Action(resultTag = "InspectedLogginData")
		public double execute() {
			return ((TimerData) timeTag.getValue()).getCpuDuration();
		}

	}

}
