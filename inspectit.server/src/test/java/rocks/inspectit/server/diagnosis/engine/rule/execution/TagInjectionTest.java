package rocks.inspectit.server.diagnosis.engine.rule.execution;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;
import rocks.inspectit.server.diagnosis.engine.rule.api.InjectionType;
import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDummy;
import rocks.inspectit.server.diagnosis.engine.rule.definition.TagInjection;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.engine.util.SessionVariables;
import rocks.inspectit.shared.all.testbase.TestBase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @author Claudio Waldvogel
 */
public class TagInjectionTest extends TestBase {

    @Mock
    RuleInput input;

    @Mock
    RuleDefinition definition;

    @Mock
    SessionVariables variables;

    @Mock(name = "instance")
    RuleDummy dummy;

    @InjectMocks
    ExecutionContext context;

    public static class Execute extends TagInjectionTest {

        @Test
        public void shouldInjectValue() {
            //prepare
            when(input.getUnraveled()).thenReturn(Tags.tags("T1", null, "InjectedValue"));
            //execute
            new TagInjection("T1", RuleDummy.tagStringValueField()).execute(context);
            //verify
            assertThat(dummy.tagStringValueField, is("InjectedValue"));
        }

        @Test
        public void shouldInjectTag() {
            //prepare
            when(input.getUnraveled()).thenReturn(Tags.tags("T1", null, "InjectedValue"));
            //execute
            new TagInjection("T1", RuleDummy.tagAsTagField(), InjectionType.BY_TAG).execute(context);
            //verify
            assertThat(dummy.tagAsTagField, is(Tags.tag("T1", "InjectedValue")));
        }
    }
}
