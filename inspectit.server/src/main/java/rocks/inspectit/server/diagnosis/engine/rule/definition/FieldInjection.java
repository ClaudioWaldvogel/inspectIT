/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule.definition;

import java.lang.reflect.Field;

import com.google.common.base.Preconditions;

import rocks.inspectit.server.diagnosis.engine.rule.execution.ExecutionContext;

/**
 * @author Claudio Waldvogel
 *
 */
public abstract class FieldInjection {

	private final Field target;

	/**
	 * @param target
	 */
	public FieldInjection(Field target) {
		this.target = Preconditions.checkNotNull(target, "The target field must not be null.");
		// Ensure that field is accessible
		this.target.setAccessible(true);
	}

	public abstract void execute(ExecutionContext context);

	/**
	 * Gets {@link #target}.
	 *
	 * @return {@link #target}
	 */
	public Field getTarget() {
		return target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "FieldInjection [target=" + target + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		FieldInjection other = (FieldInjection) obj;
		if (target == null) {
			if (other.target != null) {
				return false;
			}
		} else if (!target.equals(other.target)) {
			return false;
		}
		return true;
	}

}
