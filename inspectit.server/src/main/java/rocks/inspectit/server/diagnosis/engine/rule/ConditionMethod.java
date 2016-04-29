package rocks.inspectit.server.diagnosis.engine.rule;

import com.google.common.base.Strings;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Defines a condition method of a rule implementation. A <code>ConditionMethod</code> reflects the {@link Condition} annotation.
 *
 * @author Claudio Waldvogel
 * @see Condition
 */
public class ConditionMethod {

	/**
	 * The name of the condition.
	 *
	 * @see Condition#name()
	 */
	private final String name;

	/**
	 * A hint why the condition failed.
	 *
	 * @see Condition#hint()
	 */
	private final String hint;

	/**
	 * The actual condition implementation of a rule. As shown in listing, a valid condition method implementation has no parameters and returns a boolean.
	 * <p/>
	 * <pre>
	 * {@code
	 *     @literal @Condition(name = "MyCondition", hint = "Some useful information")
	 *     public boolean condition(){
	 *         return true | false;
	 *     }
	 * }
	 * </pre>
	 */
	private final Method method;

	/**
	 * Default Constructor
	 *
	 * @param name
	 * 		The name of the condition
	 * @param hint
	 * 		A hint why the condition failed
	 * @param method
	 * 		The actual backing implementation of the condition method
	 */
	public ConditionMethod(String name, String hint, Method method) {
		this.method = checkNotNull(method);
		this.name = fixName(name, this.method.getName());
		this.hint = hint;
	}

	/**
	 * Executes this <code>ConditionMethod</code>. If the #method does not succeed a <code>ConditionFailure</code> is returned. Otherwise null is returned.
	 *
	 * @param context
	 * 		The current executing <code>ExecutionContext</code>
	 * @return A <code>ConditionFailure</code> if condition fails, null otherwise
	 * @see ExecutionContext
	 * @see ConditionFailure
	 */
	public ConditionFailure execute(ExecutionContext context) {
		try {
			boolean valid = (boolean) getMethod().invoke(context.getInstance());
			if (!valid) {
				// Store information about the failed condition for later usage
				return new ConditionFailure(getName(), getHint());
			}
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new RuleExecutionException("Invocation of condition method failed.", context, e);
		}
		return null;
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
	 * Gets {@link #hint}.
	 *
	 * @return {@link #hint}
	 */
	public String getHint() {
		return hint;
	}

	/**
	 * Gets {@link #method}.
	 *
	 * @return {@link #method}
	 */
	public Method getMethod() {
		return method;
	}

	//-------------------------------------------------------------
	// Methods: internals
	//-------------------------------------------------------------

	/**
	 * Utility method to ensure a valid condition name.
	 *
	 * @param name
	 * 		The preferred name
	 * @param alternative
	 * 		An alternative name
	 * @return A not empty, not null String
	 * @throws IllegalArgumentException if both Strings or null or empty
	 */
	private String fixName(String name, String alternative) {
		if (Strings.isNullOrEmpty(name)) {
			if (Strings.isNullOrEmpty(alternative)) {
				throw new IllegalArgumentException("Alternative must not be null or empty.");
			}
			return alternative;
		} else {
			return name;
		}
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	@Override
	public String toString() {
		return "ConditionMethod{" + "name='" + name + '\'' + ", hint='" + hint + '\'' + ", method=" + method + '}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hint == null) ? 0 : hint.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ConditionMethod other = (ConditionMethod) obj;
		if (hint == null) {
			if (other.hint != null) {
				return false;
			}
		} else if (!hint.equals(other.hint)) {
			return false;
		}
		if (method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!method.equals(other.method)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
