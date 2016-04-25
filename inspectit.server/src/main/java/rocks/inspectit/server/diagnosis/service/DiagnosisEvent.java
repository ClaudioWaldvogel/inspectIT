/**
 *
 */
package rocks.inspectit.server.diagnosis.service;

import org.springframework.context.ApplicationEvent;

/**
 * @author Claudio Waldvogel
 *
 */
public class DiagnosisEvent extends ApplicationEvent {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param source
	 */
	public DiagnosisEvent(Object source) {
		super(source);
	}

}
