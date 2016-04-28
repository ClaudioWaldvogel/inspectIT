package rocks.inspectit.server.diagnosis.engine.rule.execution;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;
import rocks.inspectit.server.diagnosis.engine.rule.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDummy;
import rocks.inspectit.server.diagnosis.engine.rule.SessionVariableInjection;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;
import rocks.inspectit.shared.all.testbase.TestBase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

/**
 * @author Claudio Waldvogel
 */
public class SessionVariableInjectionTest extends TestBase {

    @Mock
    RuleInput input;

    @Mock
    RuleDefinition definition;

    @Mock
    SessionVariables variables;

    @Mock(name = "instance")
    RuleDummy ruleImpl;

    @InjectMocks
    ExecutionContext context;


    public static class Execute extends SessionVariableInjectionTest {

        @Test
        public void shouldInjectValue() {
            //prepare
            when(variables.get("var")).thenReturn(123);
            //execute
            new SessionVariableInjection("var", false, RuleDummy.sessionVariableIntField()).execute(context);
            //verify
            assertThat(ruleImpl.sessionIntVariable, is(123));
        }

        @Test
        public void shouldRespectOptional() {
            //execute
            new SessionVariableInjection("var", true, RuleDummy.sessionVariableIntField()).execute(context);
            //verify
            assertThat(ruleImpl.sessionIntVariable, is(nullValue()));
        }

        @Test(expectedExceptions = RuleExecutionException.class)
        public void shouldFailDueToMissingVariable() {
            //execute
            new SessionVariableInjection("var", false, RuleDummy.sessionVariableIntField()).execute(context);
        }
    }
}
