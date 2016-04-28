package rocks.inspectit.server.diagnosis.engine.rule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to define a class as Rule implementation. The <code>Rule</code> annotation is only valid for <code>Types</code>. The following examples illustrates the common skeleton of a rule
 * implementation. The top level <code>Rule</code> annotation is used to define the name, a description of the rule, and a fireCondition. The <code>fireCondition</code> property defines the *
 * condition at what time the rule will be executed. This property is translated to a {@link rocks.inspectit.server.diagnosis.engine.rule.FireCondition} instance. As shown a rule is completed by
 * {@link TagValue}, {@link SessionVariable}, {@link Condition}, and {@link Action} annotations.
 * <p/>
 * <pre>
 * {@code
 * @literal @Rule(name = "MyRule", description = "A description", fireCondition = { "Tag1" })
 * public static class MyRule {
 *
 * 	@literal @literal @TagValue(type = "Tag1", InjectionStrategy.BY_VALUE)
 * 	private String tag1Value;
 *
 * 	@literal @literal @SessionVariable(name = "var", optional = false)
 * 	private String sessionVariable;
 *
 * 	@literal @literal @Condition(name = "check", hint = "How to pass the condition.")
 * 	public boolean codition() {
 * 		return true;
 * 	}
 *
 * 	@literal@ literal @Action(resultTag = "Tag2",resultQuantity = Quantity.SINGLE)
 * 	public String action() {
 * 		return "A new Value";
 * 	}
 * }
 * </pre>
 *
 * @author Claudio Waldvogel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Rule {

	/**
	 * @return The name of the rule
	 */
	String name() default "";

	/**
	 * @return A description what the purpose of this rule is
	 */
	String description() default "";

	/**
	 * The fireCondition defines which {@link rocks.inspectit.server.diagnosis.engine.tag.Tag}s have to be already available before this rule can executed. If the fireCondition property is omitted,
	 * the fireCondition is constructed from all <code>TagValues</code>
	 *
	 * @return A list of tag types.
	 * @see TagValue
	 */
	String[] fireCondition() default {};
}
