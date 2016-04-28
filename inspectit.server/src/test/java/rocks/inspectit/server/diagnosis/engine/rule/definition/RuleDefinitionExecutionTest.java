/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule.definition;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.engine.util.SessionVariables;
import rocks.inspectit.shared.all.testbase.TestBase;

import java.util.List;

/**
 * @author Claudio Waldvogel
 *
 */
public class RuleDefinitionExecutionTest extends TestBase {

	@Mock
	ActionMethod actionMethod;

	@Mock
	TagInjection tagInjection;

	@Mock
	TagInjection otherTagInjection;

	@InjectMocks
	public RuleDefinition rule;
	

	public static class Execute extends RuleDefinitionExecutionTest {

		@Test
		public void test() {
			rule.implementation = RuleDummy.class;

			ActionMethod definition = rule.getActionMethod();
			List<TagInjection> tagInjections2 = rule.getTagInjections();

			Tag rootTag = Tags.rootTag("input");
			RuleOutput output = rule.execute(new RuleInput(rootTag), new SessionVariables());
		}

	}

	public static class RuleDummy {

		public Object tagValue;

		public Object otherTagValue;

		public boolean failConidtion() {
			return false;
		}

		public boolean successCondiction() {
			return true;
		}

		public Object action() {
			return "action";
		}

	}

}
