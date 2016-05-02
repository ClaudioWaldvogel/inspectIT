/**
 *
 */
package rocks.inspectit.server.diagnosis.engine;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.DefaultSessionResult;
import rocks.inspectit.server.diagnosis.engine.session.DefaultSessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.ISessionCallback;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Claudio Waldvogel
 */
public class DiagnosisEngineTest {

    @Test
    public void testScanner() {

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        // Filter to include only classes that have a particular annotation.
        provider.addIncludeFilter(new AnnotationTypeFilter(Rule.class));
        provider.addExcludeFilter(new RegexPatternTypeFilter(Pattern.compile("rocks.inspectit.server.diagnosis.engine.rule.execution.*")));
        // Find classes in the given package (or subpackages)
        Set<BeanDefinition> beans = provider.findCandidateComponents("rocks.inspectit.server.diagnosis.engine.rule");
        for (BeanDefinition bd : beans) {
            try {
                if (bd instanceof AbstractBeanDefinition) {
                    Class<?> aClass = ((AbstractBeanDefinition) bd).resolveBeanClass(DiagnosisEngineTest.this.getClass().getClassLoader());
                    RuleDefinition define = Rules.define(aClass);
                    System.out.println(define);
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testEngine() {

        final List<DefaultSessionResult<String>> results = new ArrayList<>();

        DiagnosisEngineConfiguration<String, DefaultSessionResult<String>> configuration =
                new DiagnosisEngineConfiguration<String, DefaultSessionResult<String>>()
                        .setNumSessionWorkers(1)
                .setNumRuleWorkers(1).setRuleClasses(newHashSet(R1.class, R2.class, R3.class)).setStorageClass(DefaultRuleOutputStorage.class)
                .setResultCollector(new DefaultSessionResultCollector<String>())
                .setSessionCallback(new ISessionCallback<DefaultSessionResult<String>>() {
                    @Override
                    public void onSuccess(DefaultSessionResult<String> result) {
                        System.out.println("Session completed!");
                        results.add(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        System.out.println("Session failed!");
                        t.printStackTrace();
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