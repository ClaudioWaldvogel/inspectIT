/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.session;

import org.testng.annotations.Test;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.*;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

import java.util.Collection;
import java.util.Iterator;

import static org.testng.Assert.assertEquals;

/**
 * @author Claudio Waldvogel
 *
 */
public class DiagnosisSessionTest {

	@Test
	public void testSession() {

		final Session<String, DefaultSessionResult<String>> session = Session.<String, DefaultSessionResult<String>> builder().setNumRuleWorkers(1).setShutDownTimeout(2)
				.setRuleDefinitions(Rules.define(R1.class, R2.class, R3.class)).setStorageClass(DefaultRuleOutputStorage.class).setSessionResultCollector(new DefaultSessionResultCollector<String>())
				.build();

		DefaultSessionResult<String> result = session.activate("Input").process().collectResults();

		session.passivate().destroy();

		String input = result.getInput();

		assertEquals(result.getEndTags().values().size(), 2);

		Collection<Tag> collection = result.getEndTags().get("Tag2");

		// List<Tag> tags = ((ArrayList<Tag>) collection);

		Iterator<Tag> iterator = collection.iterator();

		boolean equals = iterator.next().equals(iterator.next());

	}

	public static class R1 {

		@TagValue(type = Tags.ROOT_TAG, injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Action(resultTag = "Tag1")
		public String action() {
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
