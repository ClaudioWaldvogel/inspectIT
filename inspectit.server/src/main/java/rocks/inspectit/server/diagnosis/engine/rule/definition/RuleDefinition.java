package rocks.inspectit.server.diagnosis.engine.rule.definition;

import com.google.common.collect.Sets;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.util.SessionVariables;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.tryInstantiate;

/**
 * @author Claudio Waldvogel
 */
public class RuleDefinition {

	public static final String NON_DESCRIPTION = "Not available";

	String name;
	String description;
	Class<?> implementation;
	FireCondition fireCondition;
	List<TagInjection> tagInjections;
	ActionMethod actionMethod;
	List<ConditionMethod> conditionMethods;
	List<SessionVariableInjection> variableInjections;

	/**
	 *
	 * @param name
	 * @param description
	 * @param implementation
	 * @param fireCondition
	 * @param conditionMethods
	 * @param actionMethod
	 * @param tagInjections
	 * @param variableInjections
	 */
	public RuleDefinition(String name, String description, Class<?> implementation, FireCondition fireCondition, List<ConditionMethod> conditionMethods, ActionMethod actionMethod,
			List<TagInjection> tagInjections, List<SessionVariableInjection> variableInjections) {
		this.name = name;
		this.description = description;
		this.implementation = implementation;
		this.fireCondition = fireCondition;
		this.tagInjections = tagInjections;
		this.actionMethod = actionMethod;
		this.conditionMethods = conditionMethods;
		this.variableInjections = variableInjections;

	}

	// -------------------------------------------------------------
	// Methods: Execution
	// -------------------------------------------------------------

	public Collection<RuleOutput> execute(Collection<RuleInput> inputs, SessionVariables variables) {
		checkNotNull(inputs, "The RuleInputs must not be null!");

		Iterator<RuleInput> iterator = inputs.iterator();
		Set<RuleOutput> outputs = Sets.newHashSet();

		while (iterator.hasNext()) {
			outputs.add(execute(iterator.next(), variables));
		}
		return outputs;
	}

	public RuleOutput execute(RuleInput input, SessionVariables variables) {
		checkNotNull(input, "The RuleInput must not be null!");
		// and there must be same amount of tags as injections points
		checkArgument(input.getUnraveled().size() == getTagInjections().size(), "Invalid input " + "definition. Uneven quantity of input tags and @Value injection definitions.");

		// Settle a new ExecutionContext for this run
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

		// Finally execute the rule's actual action if not conditions failed
		Collection<Tag> tags = Sets.newHashSet();
		if (conditionFailures.size() == 0) {
			tags = getActionMethod().execute(ctx);
		}
		// Deliver result
		return new RuleOutput(getName(), getActionMethod().getResultTag(), conditionFailures, tags);
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
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		RuleDefinition that = (RuleDefinition) o;

		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
			return false;
		}
		if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null) {
			return false;
		}
		if (getImplementation() != null ? !getImplementation().equals(that.getImplementation()) : that.getImplementation() != null) {
			return false;
		}
		if (getFireCondition() != null ? !getFireCondition().equals(that.getFireCondition()) : that.getFireCondition() != null) {
			return false;
		}
		if (getTagInjections() != null ? !getTagInjections().equals(that.getTagInjections()) : that.getTagInjections() != null) {
			return false;
		}
		if (getActionMethod() != null ? !getActionMethod().equals(that.getActionMethod()) : that.getActionMethod() != null) {
			return false;
		}
		return getConditionMethods() != null ? getConditionMethods().equals(that.getConditionMethods()) : that.getConditionMethods() == null;

	}

	@Override
	public int hashCode() {
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
		result = 31 * result + (getImplementation() != null ? getImplementation().hashCode() : 0);
		result = 31 * result + (getFireCondition() != null ? getFireCondition().hashCode() : 0);
		result = 31 * result + (getTagInjections() != null ? getTagInjections().hashCode() : 0);
		result = 31 * result + (getActionMethod() != null ? getActionMethod().hashCode() : 0);
		result = 31 * result + (getConditionMethods() != null ? getConditionMethods().hashCode() : 0);
		return result;
	}

}
