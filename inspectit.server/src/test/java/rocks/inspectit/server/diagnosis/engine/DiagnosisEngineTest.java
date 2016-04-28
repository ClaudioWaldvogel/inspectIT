/**
 *
 */
package rocks.inspectit.server.diagnosis.engine;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.DefaultSessionResult;
import rocks.inspectit.server.diagnosis.engine.session.DefaultSessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.ISessionResultHandler;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Claudio Waldvogel
 */
public class DiagnosisEngineTest {

    @Test
    public void testEngine() {

        final List<DefaultSessionResult<String>> results = new ArrayList<>();

        DiagnosisEngineConfiguration<String, DefaultSessionResult<String>> configuration = new DiagnosisEngineConfiguration<String, DefaultSessionResult<String>>().setNumSessionWorkers(1)
                .setNumRuleWorkers(1)
                .setRuleClasses(newHashSet(R1.class, R2.class, R3.class)).setStorageClass(DefaultRuleOutputStorage.class).setResultCollector(new DefaultSessionResultCollector<String>())
                .setResultHandler(new ISessionResultHandler<DefaultSessionResult<String>>() {
                    @Override
                    public void handle(DefaultSessionResult<String> result) {
                        results.add(result);
                    }
                });

        DiagnosisEngine<String, DefaultSessionResult<String>> diagnosisEngine = new DiagnosisEngine<>(configuration);

        try {
            diagnosisEngine.analyze("Trace");
            diagnosisEngine.analyze("Trace2");
            diagnosisEngine.shutdown(true);
            Assert.assertEquals(results.size(), 2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class R1 {

        @TagValue(type = Tags.ROOT_TAG, injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
        private String input;

        @Action(resultTag = "Tag1")
        public String action() {
            if (input.equals("Trace")) {
                throw new NullPointerException();
            }
            return input + "Enhanced";
        }

    }

    public static class R2 {

        @TagValue(type = "Tag1", injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
        private String input;

        @Action(resultTag = "Tag2", resultQuantity = Action.Quantity.MULTIPLE)
        public String[] action() {
            return new String[] { input + "AgainEnhanced", input + "AgainEnhanced2" };
        }

    }

    @Rule(name = "FailingRule")
    public static class R3 {

        @TagValue(type = "Tag2", injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
        private String input;

        @Condition(name = "shouldFail", hint = "Also no problem")
        public boolean fail() {
            return false;
        }

        @Condition(name = "shouldAlsoFail", hint = "No problem")
        public boolean fail2() {
            return false;
        }

        @Action(resultTag = "Tag3")
        public String action() {
            return "Never executed!";
        }
    }

}