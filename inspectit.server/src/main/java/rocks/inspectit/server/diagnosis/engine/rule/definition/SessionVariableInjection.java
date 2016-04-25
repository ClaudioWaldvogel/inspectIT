/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule.definition;

import java.lang.reflect.Field;

import rocks.inspectit.server.diagnosis.engine.rule.execution.ExecutionContext;

/**
 * @author Claudio Waldvogel
 *
 */
public class SessionVariableInjection extends FieldInjection {

	private final String paramterName;
	private final boolean optional;

	public SessionVariableInjection(String paramterName, boolean optional, Field field) {
		super(field);
		this.paramterName = paramterName;
		this.optional = optional;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(ExecutionContext context) {
		Object parameter = context.getSessionParameters().get(getParamterName());
		if (parameter == null && !isOptional()) {
			// TODO define proper exceptions
			throw new IllegalArgumentException();
		}
		try {
			getTarget().set(context.getInstance(), parameter);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}

	}

	/**
	 * Gets {@link #paramterName}.
	 *
	 * @return {@link #paramterName}
	 */
	public String getParamterName() {
		return paramterName;
	}

	/**
	 * Gets {@link #optional}.
	 *
	 * @return {@link #optional}
	 */
	public boolean isOptional() {
		return optional;
	}

}
