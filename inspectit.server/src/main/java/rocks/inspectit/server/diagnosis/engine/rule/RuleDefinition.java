package rocks.inspectit.server.diagnosis.engine.rule;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.tryInstantiate;

/**
 * A <code>RuleDefinition</code> is an abstracted and generalized view of a rule implementation. Each rule implementation which is passed to the {@link
 * rocks.inspectit.server.diagnosis.engine.DiagnosisEngine} is converted in a <code>RuleDefinition</code>.
 * <p>
 * <pre>
 *  A <code>RuleDefinition</code> summarizes
 *  <ul>
 *      <li>The name of a rule</li>
 *      <li>The description of a rule</li>
 *      <li>The implementing class of a rule</li>
 *      <li>The <code>FireCondition</code> of a rule</li>
 *      <li>The <code>TagInjection</code>s of a rule</li>
 *      <li>The <code>SessionVariableInjection</code>s of a rule</li>
 *      <li>The <code>ConditionMethod</code>s of a rule</li>
 *  </ul>
 * </pre>
 *
 * @author Claudio Waldvogel
 * @see FireCondition
 * @see TagInjection
 * @see SessionVariables
 * @see ConditionMethod
 */
public class RuleDefinition {

	/**
	 * The default description of a rule.
	 */
	public static final String EMPTY_DESCRIPTION = "EMPTY";

	/**
	 * The name of this rule.
	 */
	private String name;

	/**
	 * The description of this rule
	 */
	private String description;

	/**
	 * The backing implementation class of this rule.
	 */
	private Class<?> implementation;

	/**
	 * The <code>FireCondition</code> of this rule.
	 */
	private FireCondition fireCondition;

	/**
	 * The required <code>TagInjection</code>s of this rule.
	 *
	 * @see TagInjection
	 */
	private List<TagInjection> tagInjections;

	/**
	 * The required <code>SessionVariableInjection</code>s of this rule.
	 *
	 * @see SessionVariableInjection
	 */
	private List<SessionVariableInjection> variableInjections;

	/**
	 * The <code>ConditionMethod</code>s of this rule.
	 *
	 * @see ConditionMethod
	 */
	private List<ConditionMethod> conditionMethods;

	/**
	 * The <code>ActionMethod</code>s of this rule.
	 *
	 * @see ActionMethod
	 */
	private ActionMethod actionMethod;

	/**
	 * Default constructor
	 *
	 * @param name
	 * 		The name of this rule. Must not be null.
	 * @param description
	 * 		The description of this rule. Must not be null.
	 * @param implementation
	 * 		The backing rule implementation. Must not be null.
	 * @param fireCondition
	 * 		The FireCondition of this rule. Must not be null.
	 * @param conditionMethods
	 * 		The ConditionMethod of this rule. Must not be null.
	 * @param actionMethod
	 * 		The actionMethod of this rule. Must not be null.
	 * @param tagInjections
	 * 		The TagInjections of this rule. Must not be null.
	 * @param variableInjections
	 * 		the SessionVariableInjections of this rule. Must not be null.
	 */
	public RuleDefinition(String name, String description, Class<?> implementation, FireCondition fireCondition, List<ConditionMethod> conditionMethods, ActionMethod actionMethod,
			List<TagInjection> tagInjections, List<SessionVariableInjection> variableInjections) {
		this.implementation = checkNotNull(implementation);
		this.fireCondition = checkNotNull(fireCondition);
		this.tagInjections = checkNotNull(tagInjections);
		this.actionMethod = checkNotNull(actionMethod);
		this.conditionMethods = checkNotNull(conditionMethods);
		this.variableInjections = checkNotNull(variableInjections);
		this.name = StringUtils.defaultIfEmpty(name, implementation.getName());
		this.description = StringUtils.defaultIfEmpty(description, EMPTY_DESCRIPTION);
	}

	// -------------------------------------------------------------
	// Methods: RuleExecution
	// -------------------------------------------------------------

	/**
	 * Executes this <code>RuleDefinition</code> in 6 steps.
	 * <p/>
	 * <pre>
	 * 1. The raw class which implements this <code>RuleDefinition</code> is instantiated and wrapped in a new <code>ExecutionContext</code>.
	 * 2. All <code>TagInjection</code>s are executed.
	 * 3. All <code>SessionVariableInjection</code>s are executed
	 * 4. All <code>ConditionMethod</code>s are executed.
	 * 5. If all <code>ConditionMethod</code>s succeed, the <code>ActionMethod</code> is executed.
	 * 6. A new <code>RuleOutput</code> is created and returned
	 * </pre>
	 *
	 * @param input
	 * 		The <code>RuleInput</code> to be processed. Must not null.
	 * @param variables
	 * 		The <code>SessionVariables</code>. Must not null.
	 * @return A new <code>RuleOutput</code>
	 * @throws RuntimeException
	 * @see ExecutionContext
	 * @see RuleInput
	 * @see RuleOutput
	 * @see SessionVariables
	 */
	public RuleOutput execute(RuleInput input, SessionVariables variables) {
		checkNotNull(input, "The RuleInput must not be null!");
		checkNotNull(variables, "The SessionVariables must not be null!");

		/*// and there must be same amount of tags as injections points
		checkArgument(input.getUnraveled().size() == getTagInjections().size(), "Invalid input " + "definition. Uneven quantity of input tags and @Value injection definitions.");*/

		// Create a new ExecutionContext for this run
		ExecutionContext ctx = new ExecutionContext(this, tryInstantiate(getImplementation()), input, variables);

		// Inject tags
		for (TagInjection injection : getTagInjections()) {
			injection.execute(ctx);
		}

		// Inject session variables
		for (SessionVariableInjection injection : getSessionVariableInjections()) {
			injection.execute(ctx);
		}

		// Check conditions
		Collection<ConditionFailure> conditionFailures = new ArrayList<>();
		for (ConditionMethod conditionMethod : getConditionMethods()) {
			ConditionFailure failure = conditionMethod.execute(ctx);
			if (failure != null) {
				conditionFailures.add(failure);
			}
		}

		//If no condition failed, execute the actual action
		Collection<Tag> tags = Lists.newArrayList();
		if (conditionFailures.size() == 0) {
			tags = getActionMethod().execute(ctx);
		}

		// Deliver result
		return new RuleOutput(getName(), getActionMethod().getResultTag(), conditionFailures, tags);
	}

	/**
	 * Convenience method to execute this <code>RuleDefinition</code> for several <code>RuleInput</code>s. The amount of <code>RuleInput</code>s equals the amount of executions of this
	 * <code>RuleDefinition</code>. Each <code>RuleInput</code> concludes in a invocation of {@link #execute(RuleInput, SessionVariables)}.
	 *
	 * @param inputs
	 * 		A collection of <code>RuleInput</code> to be processed.
	 * @param variables
	 * 		The <code>SessionVariables</code>
	 * @return A collection of <code>RuleOutput</code>s.
	 * @see RuleInput
	 * @see RuleOutput
	 * @see SessionVariables
	 */
	public Collection<RuleOutput> execute(Collection<RuleInput> inputs, SessionVariables variables) {
		checkNotNull(inputs, "The RuleInputs must not be null!");

		Iterator<RuleInput> iterator = inputs.iterator();
		Set<RuleOutput> outputs = Sets.newHashSet();

		while (iterator.hasNext()) {
			outputs.add(execute(iterator.next(), variables));
		}
		return outputs;
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets {@link #description}.
	 *
	 * @return {@link #description}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets {@link #implementation}.
	 *
	 * @return {@link #implementation}
	 */
	public Class<?> getImplementation() {
		return implementation;
	}

	/**
	 * Gets {@link #fireCondition}.
	 *
	 * @return {@link #fireCondition}
	 */
	public FireCondition getFireCondition() {
		return fireCondition;
	}

	/**
	 * Gets {@link #tagInjections}.
	 *
	 * @return {@link #tagInjections}
	 */
	public List<TagInjection> getTagInjections() {
		return tagInjections;
	}

	/**
	 * Gets {@link #variableInjections}.
	 *
	 * @return {@link #variableInjections}
	 */
	public List<SessionVariableInjection> getSessionVariableInjections() {
		return variableInjections;
	}

	/**
	 * Gets {@link #actionMethod}.
	 *
	 * @return {@link #actionMethod}
	 */
	public ActionMethod getActionMethod() {
		return actionMethod;
	}

	/**
	 * Gets {@link #conditionMethods}.
	 *
	 * @return {@link #conditionMethods}
	 */
	public List<ConditionMethod> getConditionMethods() {
		return conditionMethods;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	@Override
	public String toString() {
		return "RuleDefinition{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", implementation=" + implementation + ", fireCondition=" + fireCondition + ", injectionPoints="
				+ tagInjections + ", actionMethod=" + actionMethod + ", conditionMethods=" + conditionMethods + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		RuleDefinition that = (RuleDefinition) o;

		return new EqualsBuilder().append(getName(), that.getName()).append(getDescription(), that.getDescription()).append(getImplementation(), that.getImplementation())
				.append(getFireCondition(), that.getFireCondition()).append(getTagInjections(), that.getTagInjections()).append(variableInjections, that.variableInjections)
				.append(getConditionMethods(), that.getConditionMethods()).append(getActionMethod(), that.getActionMethod()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getName()).append(getDescription()).append(getImplementation()).append(getFireCondition()).append(getTagInjections()).append(variableInjections)
				.append(getConditionMethods()).append(getActionMethod()).toHashCode();
	}
}
