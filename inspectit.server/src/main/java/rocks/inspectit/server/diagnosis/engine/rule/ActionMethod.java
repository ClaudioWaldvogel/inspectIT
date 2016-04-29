package rocks.inspectit.server.diagnosis.engine.rule;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

import java.lang.reflect.Method;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the action method of a rule. An <code>ActionMethod</code> reflects the {@link Action} annotation.
 *
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 * @see Action
 */
public class ActionMethod {

	/**
	 * The {@link Method} to be invoked.
	 */
	private final Method method;

	/**
	 * The tag type this action produces.
	 *
	 * @see Action#resultTag()
	 */
	private final String resultTag;

	/**
	 * The output Quantity.
	 *
	 * @see Action#resultQuantity()
	 */
	private final Action.Quantity resultQuantity;

	/**
	 * Default Constructor
	 *
	 * @param method
	 * 		The method to be invoked
	 * @param resultTag
	 * 		The type of tags this action produces
	 * @param resultQuantity
	 * 		The result Quantity
	 */
	public ActionMethod(Method method, String resultTag, Action.Quantity resultQuantity) {
		this.method = checkNotNull(method, "The method must not be null.");
		this.resultTag = checkNotNull(resultTag, "The result tag must not be null.");
		this.resultQuantity = checkNotNull(resultQuantity, "The output quantity must not be null.");
	}

	// -------------------------------------------------------------
	// Methods: Execution
	// -------------------------------------------------------------

	/**
	 * Executes the action.
	 *
	 * @param context
	 * 		The current executing <code>ExecutionContext</code>
	 * @return A collection of <code>Tags</code>s
	 * @throws RuleExecutionException
	 * 		RuleExecutionException is raised if the execution fails
	 * @see ExecutionContext
	 * @see Tag
	 */
	public Collection<Tag> execute(ExecutionContext context) {
		try {
			Object result = getMethod().invoke(context.getInstance());
			return transform(result, context);
		} catch (Exception e) {
			throw new RuleExecutionException("Failed to invoke action method.", context, e);
		}
	}

	/**
	 * Transforms the result of a rule to a collection of <code>Tag</code>s. How the result is transformed is controlled by the #resultQuantity property.
	 *
	 * @param result
	 * 		The result to be transformed
	 * @param context
	 * 		The <code>ExecutionContext</code> enclosing this execution
	 * @return A collection of <code>Tag</code>s
	 * @see Tag
	 * @see ExecutionContext
	 */
	private Collection<Tag> transform(Object result, ExecutionContext context) {
		Collection<Tag> transformed = Lists.newArrayList();
		if (result != null) {
			switch (getResultQuantity()) {
			case MULTIPLE:
				Object[] values;
				if (result.getClass().isArray()) {
					values = (Object[]) result;
				} else if (result instanceof Iterable<?>) {
					values = Iterables.toArray((Iterable<?>) result, Object.class);
				} else {
					throw new RuleExecutionException("If resultQuantity is MULTIPLE ensure that either an Array or a Collection is defined as return value", context);
				}
				transformed.addAll(Tags.tags(getResultTag(), context.getRuleInput().getRoot(), values));
				break;
			case SINGLE:
			default:
				transformed.add(Tags.tag(getResultTag(), result, context.getRuleInput().getRoot()));
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
	 * Gets {@link #resultQuantity}.
	 *
	 * @return {@link #resultQuantity}
	 */
	public Action.Quantity getResultQuantity() {
		return resultQuantity;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	@Override
	public String toString() {
		return "ActionMethod{" + "method=" + method + ", resultTag='" + resultTag + '\'' + ", resultQuantity=" + resultQuantity + '}';
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
		return getResultQuantity() == method1.getResultQuantity();

	}

	@Override
	public int hashCode() {
		int result = getMethod() != null ? getMethod().hashCode() : 0;
		result = 31 * result + (getResultTag() != null ? getResultTag().hashCode() : 0);
		result = 31 * result + (getResultQuantity() != null ? getResultQuantity().hashCode() : 0);
		return result;
	}
}
