package rocks.inspectit.server.diagnosis.engine.rule.definition;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.tryInstantiate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import rocks.inspectit.server.diagnosis.engine.rule.execution.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class RuleDefinition {

	private final String name;
	private final String description;
	private final Class<?> implementation;
	private final FireCondition fireCondition;
	private final List<TagInjection> injectionPoints;
	private final ActionMethod actionMethod;
	private final List<ConditionMethod> conditionMethods;

	public RuleDefinition(String name, String description, Class<?> implementation, FireCondition fireCondition, List<ConditionMethod> conditionMethods, ActionMethod actionMethod,
			List<TagInjection> injectionPoints) {
		this.implementation = implementation;
		this.fireCondition = fireCondition;
		this.name = name;
		this.description = description;
		this.injectionPoints = injectionPoints;
		this.actionMethod = actionMethod;
		this.conditionMethods = conditionMethods;
	}

	// -------------------------------------------------------------
	// Methods: Execution
	// -------------------------------------------------------------

	public Collection<RuleOutput> execute(Collection<RuleInput> inputs) {
		checkNotNull(inputs, "The RuleInputs must not be null!");

		Iterator<RuleInput> iterator = inputs.iterator();
		Set<RuleOutput> outputs = Sets.newHashSet();

		while (iterator.hasNext()) {
			outputs.add(execute(iterator.next()));
		}
		return outputs;
	}

	public RuleOutput execute(RuleInput input) {

		checkNotNull(input, "The RuleInputs must not be null!");
		// and there must be same amount of tags as injections points
		checkArgument(input.getUnraveled().size() == getInjectionPoints().size(), "Invalid input " + "definition. Uneven quantity of input tags and @Value injection definitions.");

		// Settle a new ExecutionContext for this run
		ExecutionContext ctx = new ExecutionContext(this, tryInstantiate(getImplementation()), input);

		// Inject tags
		for (TagInjection injection : getInjectionPoints()) {
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

		// Finally execute tne rule's actual action if not conditions failed
		Set<Tag> tags = Sets.newHashSet();
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
	 * Gets {@link #injectionPoints}.
	 *
	 * @return {@link #injectionPoints}
	 */
	public List<TagInjection> getInjectionPoints() {
		return injectionPoints;
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
				+ injectionPoints + ", actionMethod=" + actionMethod + ", conditionMethods=" + conditionMethods + '}';
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
		if (getInjectionPoints() != null ? !getInjectionPoints().equals(that.getInjectionPoints()) : that.getInjectionPoints() != null) {
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
		result = 31 * result + (getInjectionPoints() != null ? getInjectionPoints().hashCode() : 0);
		result = 31 * result + (getActionMethod() != null ? getActionMethod().hashCode() : 0);
		result = 31 * result + (getConditionMethods() != null ? getConditionMethods().hashCode() : 0);
		return result;
	}

}
