package rocks.inspectit.server.diagnosis.engine.rule;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class ActionMethod {

	private final Method method;
	private final String resultTag;
	private final Action.Quantity outputQuantity;

	public ActionMethod(Method method, String resultTag, Action.Quantity outputQuantity) {
		this.method = checkNotNull(method, "The method must not be null.");
		this.resultTag = checkNotNull(resultTag, "The result tag must not be null.");
		this.outputQuantity = checkNotNull(outputQuantity, "The output quantity must not be null.");
	}

	// -------------------------------------------------------------
	// Methods: Execution
	// -------------------------------------------------------------

	public Collection<Tag> execute(ExecutionContext context) {
		try {
			Object result = getMethod().invoke(context.getInstance());
			return transform(result, context);
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuleExecutionException("Failed to invoke action method.", context, e);
		}
	}

	private Collection<Tag> transform(Object outcome, ExecutionContext context) {
		Collection<Tag> transformed = Lists.newArrayList();
		if (outcome != null) {
			switch (getOutputQuantity()) {
			case SINGLE:
				transformed.add(Tags.tag(getResultTag(), outcome, context.getRuleInput().getRoot()));
				break;
			case MULTIPLE:
			default:
				Object[] values;
				if (outcome.getClass().isArray()) {
					values = (Object[]) outcome;
				} else if (outcome instanceof Iterable<?>) {
					values = Iterables.toArray((Iterable<?>) outcome, Object.class);
				} else {
					throw new RuleExecutionException("If resultQuantity is MULTIPLE ensure that either an Array or a Collection is defined as return value", context);
				}
				transformed.addAll(Tags.tags(getResultTag(), context.getRuleInput().getRoot(), values));
			}
		}
		return transformed;
	}

	// -------------------------------------------------------------u
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #method}.
	 *
	 * @return {@link #method}
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * Gets {@link #resultTag}.
	 *
	 * @return {@link #resultTag}
	 */
	public String getResultTag() {
		return resultTag;
	}

	/**
	 * Gets {@link #outputQuantity}.
	 *
	 * @return {@link #outputQuantity}
	 */
	public Action.Quantity getOutputQuantity() {
		return outputQuantity;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	@Override
	public String toString() {
		return "ActionMethod{" + "method=" + method + ", resultTag='" + resultTag + '\'' + ", outputQuantity=" + outputQuantity + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ActionMethod method1 = (ActionMethod) o;

		if (getMethod() != null ? !getMethod().equals(method1.getMethod()) : method1.getMethod() != null) {
			return false;
		}
		if (getResultTag() != null ? !getResultTag().equals(method1.getResultTag()) : method1.getResultTag() != null) {
			return false;
		}
		return getOutputQuantity() == method1.getOutputQuantity();

	}

	@Override
	public int hashCode() {
		int result = getMethod() != null ? getMethod().hashCode() : 0;
		result = 31 * result + (getResultTag() != null ? getResultTag().hashCode() : 0);
		result = 31 * result + (getOutputQuantity() != null ? getOutputQuantity().hashCode() : 0);
		return result;
	}

}
