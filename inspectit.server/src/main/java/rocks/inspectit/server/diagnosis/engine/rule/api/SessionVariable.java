/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Claudio Waldvogel
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SessionVariable {

	String name();

	boolean optional() default false;

}
