/**
 *
 */
package rocks.inspectit.server.diagnosis.engine;

import com.google.common.collect.Sets;
import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.inspectit.server.diagnosis.engine.rule.api.*;
import rocks.inspectit.server.diagnosis.engine.rule.execution.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.result.DefaultSessionResult;
import rocks.inspectit.server.diagnosis.engine.session.result.DefaultSessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.result.ISessionResultHandler;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Claudio Waldvogel
 */
public class DiagnosisEngineTest {

    @Test
    public void testEngine() {

        final List<DefaultSessionResult<String>> results = new ArrayList<>();

        DiagnosisEngineConfiguration<String, DefaultSessionResult<String>> configuration = new DiagnosisEngineConfiguration<String, DefaultSessionResult<String>>().setNumSessionWorkers(1)
                .setNumRuleWorkers(1)
                .setRuleClasses(Sets.newHashSet(R1.class, R2.class, R3.class)).setStorageClass(DefaultRuleOutputStorage.class).setResultCollector(new DefaultSessionResultCollector<String>())
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

        @TagValue(tagType = Tags.ROOT_TAG, injectionType = InjectionType.BY_VALUE)
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

        @TagValue(tagType = "Tag1", injectionType = InjectionType.BY_VALUE)
        private String input;

        @Action(resultTag = "Tag2", resultQuantity = Quantity.MULTIPLE)
        public String[] action() {
            return new String[] { input + "AgainEnhanced", input + "AgainEnhanced2" };
        }

    }

    @Rule(name = "FailingRule")
    public static class R3 {

        @TagValue(tagType = "Tag2", injectionType = InjectionType.BY_VALUE)
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