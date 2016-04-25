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
}
