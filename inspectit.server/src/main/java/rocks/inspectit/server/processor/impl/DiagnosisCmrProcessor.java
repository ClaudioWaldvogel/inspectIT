/**
 *
 */
package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import rocks.inspectit.server.diagnosis.service.DiagnosisEvent;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Claudio Waldvogel
 *
 */
public class DiagnosisCmrProcessor extends AbstractCmrDataProcessor {

	@Autowired
	private ApplicationEventPublisher dispatcher;

	public DiagnosisCmrProcessor() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		DiagnosisEvent diagnosisEvent = new DiagnosisEvent(defaultData);
		dispatcher.publishEvent(diagnosisEvent);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

}
