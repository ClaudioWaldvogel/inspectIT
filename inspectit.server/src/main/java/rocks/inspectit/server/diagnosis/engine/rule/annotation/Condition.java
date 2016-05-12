package rocks.inspectit.server.diagnosis.engine.rule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marks a method as a runtime condition for a rule. A rule can define several condition methods. If one condition fails, the rule will not be executed. At the time the condition method is
 * invoked, the rule is already ready for action in means of all {@link TagValue} and {@link SessionVariable} annotations are already processed and the corresponding values are available.
 * <p>
 *
 * <pre>
 * {@code
 *     @literal @Condition(name = "MyCondition", hint = "Some useful information")
 *     public boolean condition(){
 *         return true | false;
 *     }
 * }
 * </pre>
 *
 * @author Claudio Waldvogel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Condition {

	/**
	 * @return The name of the condition.
	 */
	String name() default "";

	/**
	 * @return Some information why the condition failed.
	 */
	String hint() default "";

}
