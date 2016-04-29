/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule.annotation;

import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used to injected a session variable to a rule. All required session variables needs to be passed to the {@link rocks.inspectit.server.diagnosis.engine.DiagnosisEngine#analyze(Object,
 * SessionVariables)}  before a new analysis is kicked off.
 *
 * @author Claudio Waldvogel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SessionVariable {

	/**
	 * @return The name of the session variable
	 */
	String name();

	/**
	 * Flag to indicate if this session variable is optional. If a session variable is not available and is not optional the analysis is stopped and the current analysis session will fail.
	 *
	 * @return flag if session variable is optional
	 */
	boolean optional() default false;
}
