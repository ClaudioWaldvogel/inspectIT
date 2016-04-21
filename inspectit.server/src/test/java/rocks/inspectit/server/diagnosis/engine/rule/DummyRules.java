/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule;

import java.util.Arrays;
import java.util.Set;

import com.google.common.collect.Sets;

import rocks.inspectit.server.diagnosis.engine.rule.api.Action;
import rocks.inspectit.server.diagnosis.engine.rule.api.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.api.InjectionType;
import rocks.inspectit.server.diagnosis.engine.rule.api.Quantity;
import rocks.inspectit.server.diagnosis.engine.rule.api.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.api.Value;
import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

/**
 * @author Claudio Waldvogel
 *
 */
@SuppressWarnings("all")
public class DummyRules {

	private static Class[] RULES = { ConsumeStartProduceTag1.class, ConsumeT1ProduceT2.class, ConsumeT1ProduceT3.class };

	public static Set<RuleDefinition> asRuleSet() {
		return Rules.define(RULES);
	}

	public static Set<Class<?>> classes() {
		return Sets.newHashSet(Arrays.<Class<?>> asList(RULES));
	}

	@Rule(name = "ConsumeStartProduceTag1")
	public static class ConsumeStartProduceTag1 {

		public static final String OUT_TAG = "Tag1";
		private Object failsOnRootValue;

		public ConsumeStartProduceTag1() {
		}

		public ConsumeStartProduceTag1(Object failsOnRootValue) {
			this.failsOnRootValue = failsOnRootValue;
		}

		@Value(tagType = Tags.ROOT_TAG, injectionType = InjectionType.BY_TAG)
		private Tag rootTag;

		@Condition(name = "SkipCondition", hint = "Must not be executed")
		public boolean condiction() {
			return !rootTag.getValue().equals(failsOnRootValue);
		}

		@Action(resultTag = OUT_TAG, resultQuantity = Quantity.MULTIPLE)
		public String[] action() {
			return new String[] { "Tag1Value1", "Tag1Value2" };
		}
	}

	@Rule(name = "ConsumeT1ProduceT2")
	public static class ConsumeT1ProduceT2 {

		public static final String OUT_TAG = "Tag2";

		@Value(tagType = ConsumeStartProduceTag1.OUT_TAG, injectionType = InjectionType.BY_TAG)
		private Tag tag1Value;

		@Value(tagType = Tags.ROOT_TAG, injectionType = InjectionType.BY_TAG)
		private Tag rootTag;

		@Action(resultTag = OUT_TAG, resultQuantity = Quantity.MULTIPLE)
		public Integer[] action() {
			return new Integer[] { 1, 2 };
		}
	}

	@Rule(name = "ConsumeT1ProduceT3")
	public static class ConsumeT1ProduceT3 {

		public static final String OUT_TAG = "Tag3";

		@Value(tagType = ConsumeStartProduceTag1.OUT_TAG, injectionType = InjectionType.BY_VALUE)
		private String tag1Value;

		@Action(resultTag = OUT_TAG, resultQuantity = Quantity.SINGLE)
		public Integer[] action() {
			return new Integer[] { 6, 7 };
		}
	}

}
