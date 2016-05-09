/**
 *
 */
package rocks.inspectit.server.diagnosis.service;

import org.springframework.context.ApplicationEvent;

/**
 * @author Claudio Waldvogel
 */
public class DiagnosisEvent extends ApplicationEvent {
	/**
	 * @param source
	 */
	public DiagnosisEvent(Object source) {
		super(source);
	}

}
