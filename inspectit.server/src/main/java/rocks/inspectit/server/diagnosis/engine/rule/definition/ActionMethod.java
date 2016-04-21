package rocks.inspectit.server.diagnosis.engine.rule.definition;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import rocks.inspectit.server.diagnosis.engine.rule.api.Quantity;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class ActionMethod {

	private final Method method;
	private final String resultTag;
	private final Quantity outputQuantity;

	public ActionMethod(Method method, String resultTag, Quantity outputQuantity) {
		this.method = checkNotNull(method, "The method must not be null.");
		this.resultTag = checkNotNull(resultTag, "The result tag must not be null.");
		this.outputQuantity = checkNotNull(outputQuantity, "The output quantity must not be null.");
	}

	// -------------------------------------------------------------
	// Methods: Execution
	// -------------------------------------------------------------

	public Set<Tag> execute(ExecutionContext context) {
		try {
			Object result = getMethod().invoke(context.getInstance());
			return transform(result, context);
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuleExecutionException("Failed to invoke action method.", e);
		}
	}

	private Set<Tag> transform(Object outcome, ExecutionContext context) {
		Set<Tag> transformed = Sets.newHashSet();
		if (outcome != null) {
			switch (getOutputQuantity()) {
			case SINGLE:
				transformed.add(new Tag(getResultTag(), outcome, context.getRuleInput().getRoot()));
				break;
			case MULTIPLE:
			default:
				Iterator<?> iterator;
				if (outcome.getClass().isArray()) {
					iterator = Iterators.forArray((Object[]) outcome);
				} else if (outcome instanceof Iterable) {
					iterator = ((Iterable<?>) outcome).iterator();
				} else {
					throw new IllegalArgumentException();
				}

				while (iterator.hasNext()) {
					transformed.add(new Tag(getResultTag(), iterator.next(), context.getRuleInput().getRoot()));
				}
				break;
			}
		}
		return transformed;
	}

	// -------------------------------------------------------------
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
	public Quantity getOutputQuantity() {
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
