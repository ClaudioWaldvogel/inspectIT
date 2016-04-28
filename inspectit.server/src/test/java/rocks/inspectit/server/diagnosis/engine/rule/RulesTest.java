/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule;

import org.testng.annotations.Test;
import rocks.inspectit.server.diagnosis.engine.rule.api.*;
import rocks.inspectit.server.diagnosis.engine.rule.definition.*;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.shared.all.testbase.TestBase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Claudio Waldvogel
 */
public class RulesTest extends TestBase {


    public static class Define extends RulesTest {

        @Test
        public void testWithRuleAnnotation() throws Exception {
            RuleDefinition definition = Rules.define(ValidAndAnnotated.class);
            assertThat(definition.getName(), is("AnnotatedRule"));
            assertThat(definition.getDescription(), is("Description"));
            assertThat(definition.getFireCondition().getTagTypes(), containsInAnyOrder("T1", "T2"));

            // Test tag injections
            TagInjection tagInjection = new TagInjection("T1", ValidAndAnnotated.class.getDeclaredField("t1AsTag"), InjectionType.BY_TAG);
            TagInjection tagInjection1 = new TagInjection("T2", ValidAndAnnotated.class.getDeclaredField("t2TagValue"), InjectionType.BY_VALUE);
            assertThat(definition.getTagInjections(), is(notNullValue()));
            assertThat(definition.getTagInjections(), containsInAnyOrder(tagInjection, tagInjection1));

            // Test session variables
            SessionVariableInjection s1 = new SessionVariableInjection("baseline", false, ValidAndAnnotated.class.getDeclaredField("baseline"));
            SessionVariableInjection s2 = new SessionVariableInjection("baseline2", true, ValidAndAnnotated.class.getDeclaredField("baseline2"));
            assertThat(definition.getSessionVariableInjections(), containsInAnyOrder(s1, s2));

            // Test action method
            assertThat(definition.getActionMethod(), is(new ActionMethod(ValidAndAnnotated.class.getDeclaredMethod("action"), "T2", Quantity.SINGLE)));

            // Test condition method
            ConditionMethod conidtionMethod = new ConditionMethod("myCondition", "No way out", ValidAndAnnotated.class.getDeclaredMethod("condition"));
            assertThat(definition.getConditionMethods(), containsInAnyOrder(conidtionMethod));

        }

        @Test
        public void testWithoutRuleAnnotation() {
            RuleDefinition definition = Rules.define(ValidNotAnnotated.class);
            assertThat(definition.getName(), is("rocks.inspectit.server.diagnosis.engine.rule.RulesTest$ValidNotAnnotated"));
            assertThat(definition.getDescription(), is(RuleDefinition.NON_DESCRIPTION));
            assertThat(definition.getFireCondition().getTagTypes(), containsInAnyOrder("T1", "root"));
        }

        @Test(expectedExceptions = RuleDefinitionException.class)
        public void testMissingActionMethod() {
            Rules.define(NoActionMethodDefined.class);
        }

        @Test(expectedExceptions = RuleDefinitionException.class)
        public void testInvalidActionMethodReturnType() {
            Rules.define(InvalidActionMethodReturnType.class);
        }

        @Test(expectedExceptions = RuleDefinitionException.class)
        public void testMultipleActionMethods() {
            Rules.define(MultipleActionMethodsDefined.class);
        }

        @Test(expectedExceptions = RuleDefinitionException.class)
        public void testMissingTagValueMethod() {
            Rules.define(NoTagValueDefined.class);
        }

    }

    @Rule(name = "AnnotatedRule", description = "Description", fireCondition = {"T1", "T2"})
    public static class ValidAndAnnotated {

        @SessionVariable(name = "baseline")
        private int baseline;

        @SessionVariable(name = "baseline2", optional = true)
        private int baseline2;

        @TagValue(tagType = "T1", injectionType = InjectionType.BY_TAG)
        public Tag t1AsTag;

        @TagValue(tagType = "T2", injectionType = InjectionType.BY_VALUE)
        public String t2TagValue;

        @Condition(name = "myCondition", hint = "No way out")
        public boolean condition() {
            return false;
        }

        @Action(resultTag = "T2")
        public String action() {
            return "executed";
        }
    }

    public static class ValidNotAnnotated {

        @TagValue(tagType = "root", injectionType = InjectionType.BY_TAG)
        private Tag rootTag;

        @TagValue(tagType = "T1")
        private String t1Value;

        @SessionVariable(name = "baseline", optional = false)
        private int baseline;

        @Action(resultTag = "T2")
        public String execute() {
            return "executed";
        }

    }

    public static class NoActionMethodDefined {
        @TagValue(tagType = "T1", injectionType = InjectionType.BY_TAG)
        public Tag t;
    }

    public static class InvalidActionMethodReturnType {
        @Action(resultTag = "T2", resultQuantity = Quantity.MULTIPLE)
        public String execute() {
            return "executed";
        }
    }

    public static class MultipleActionMethodsDefined {
        @TagValue(tagType = "T1", injectionType = InjectionType.BY_TAG)
        public Tag t;

        @Action(resultTag = "T1")
        public String execute() {
            return "executed";
        }

        @Action(resultTag = "T2")
        public String execut2() {
            return "executed";
        }
    }

    public static class NoTagValueDefined {
        @Action(resultTag = "T2")
        public String execute() {
            return "executed";
        }
    }

}
