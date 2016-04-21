/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import rocks.inspectit.server.diagnosis.engine.rule.api.Action;
import rocks.inspectit.server.diagnosis.engine.rule.api.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.api.InjectionType;
import rocks.inspectit.server.diagnosis.engine.rule.api.Quantity;
import rocks.inspectit.server.diagnosis.engine.rule.api.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.api.Value;
import rocks.inspectit.server.diagnosis.engine.rule.definition.ActionMethod;
import rocks.inspectit.server.diagnosis.engine.rule.definition.ConditionMethod;
import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.rule.definition.TagInjection;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleInput;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils;

/**
 * @author Claudio Waldvogel
 *
 */
@SuppressWarnings("PMD")
@Test
public class RulesTest {

	@Test(expectedExceptions = RuleDefinitionException.class)
	public void testMissingValueAnnotation() {
		Rules.define(MissingValueAnnotation.class);
	}

	@Test(expectedExceptions = RuleDefinitionException.class)
	public void testMultipleActionMethods() {
		Rules.define(MultipleActionMethods.class);
	}

	@Test
	public void testValidRuleDefinition() {
		RuleDefinition definition = Rules.define(SimpleRule.class);

		// Validate @Rule annotation
		assertEquals(definition.getName(), "SimpleRule");
		assertEquals(definition.getDescription(), "SimpleRule");
		assertTrue(definition.getFireCondition().getTagTypes().equals(Sets.newHashSet("T1", "T2")));

		// Validate @Value annotation
		assertEquals(definition.getInjectionPoints().size(), 2);

		for (TagInjection tagInjection : definition.getInjectionPoints()) {
			TagInjection injection = tagInjection;
			assertNotNull(injection.getTarget());
			if (injection.getTagType().equals("T1")) {
				assertEquals(injection.getInjectionType(), InjectionType.BY_VALUE);
			} else {
				assertEquals(injection.getInjectionType(), InjectionType.BY_TAG);
			}
		}

		ConditionMethod conditionMethod = definition.getConditionMethods().get(0);
		assertNotNull(conditionMethod.getMethod());
		assertEquals(conditionMethod.getName(), "Condition2");
		assertEquals(conditionMethod.getHint(), "Hint2");

		ActionMethod actionMethod = definition.getActionMethod();
		assertNotNull(actionMethod.getMethod());
		assertEquals(actionMethod.getResultTag(), "T3");
		assertEquals(actionMethod.getOutputQuantity(), Quantity.MULTIPLE);
	}

	@Test
	public void testDefinitionWithoutRuleAnnotation() {
		RuleDefinition definition = Rules.define(NoRuleAnnotation.class);
		assertEquals(definition.getName(), "rocks.inspectit.server.diagnosis.engine.rule.RulesTest$NoRuleAnnotation");
		assertEquals(definition.getDescription(), Rules.EMPTY_RULE_DESCRIPTION);
		assertEquals(definition.getFireCondition().getTagTypes(), Sets.newHashSet("T1", "T2"));
	}

	@Test
	public void testTagInjectionExecution() {
		Tag t1 = new Tag("T1", "t1Value");
		Tag t2 = new Tag("T2", "t2Value", t1);
		RuleInput ruleInput = new RuleInput(t2, Tags.unwrap(t2, Lists.newArrayList("T1", "T2")));

		RuleDefinition definition = Rules.define(ExecutionTestRule.class);

		ExecutionContext ctx = new ExecutionContext(definition, ReflectionUtils.tryInstantiate(definition.getImplementation()), ruleInput);

		for (TagInjection tagInjection : definition.getInjectionPoints()) {
			TagInjection injection = tagInjection;
			injection.execute(ctx);
		}

		ExecutionTestRule instance = (ExecutionTestRule) ctx.getInstance();
		assertEquals(instance.t1Value, "t1Value");
		assertEquals(instance.t2Value, "t2Value");
		assertTrue(instance.t2Tag instanceof Tag);
		assertEquals(instance.t2Tag.getType(), "T2");

	}

	public static class ExecutionTestRule {

		@Value(tagType = "T1")
		private String t1Value;

		@Value(tagType = "T2", injectionType = InjectionType.BY_VALUE)
		private String t2Value;

		@Value(tagType = "T2", injectionType = InjectionType.BY_TAG)
		private Tag t2Tag;

		@Action(resultTag = "Tag2", resultQuantity = Quantity.SINGLE)
		public String execute() {
			return "Tag2Value";
		}
	}

	@Rule(name = "SimpleRule", description = "SimpleRule", fireCondition = { "T1", "T2" })
	public static class SimpleRule {

		@Value(tagType = "T1")
		private String t1Value;

		@Value(tagType = "T2", injectionType = InjectionType.BY_TAG)
		private Tag t2Value;

		@Condition(name = "Condition2", hint = "Hint2")
		public boolean condition2() {
			return true;
		}

		@Action(resultTag = "T3", resultQuantity = Quantity.MULTIPLE)
		public String[] execute() {
			return new String[] { "T3_1", "T3_2" };
		}
	}

	public static class NoRuleAnnotation {

		@Value(tagType = "T1")
		private String t1Value;

		@Value(tagType = "T2")
		private String t2Value;

		@Action(resultTag = "Tag2", resultQuantity = Quantity.SINGLE)
		public String execute() {
			return "Tag2Value";
		}
	}

	public static class MultipleActionMethods {

		@Action(resultTag = "Tag3", resultQuantity = Quantity.SINGLE)
		public String execute2() {
			return "Tag2Value";
		}

		@Action(resultTag = "Tag2", resultQuantity = Quantity.SINGLE)
		public String execute() {
			return "Tag2Value";
		}
	}

	public static class MissingValueAnnotation {

		@Action(resultTag = "Tag2", resultQuantity = Quantity.SINGLE)
		public String execute() {
			return "Tag2Value";
		}
	}
}
