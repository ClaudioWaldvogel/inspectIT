/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule.definition;

import com.google.common.base.Strings;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleExecutionException;

import java.lang.reflect.Field;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author Claudio Waldvogel
 */
public class SessionVariableInjection extends FieldInjection {

    private final String variableName;
    private final boolean optional;

    public SessionVariableInjection(String variableName, boolean optional, Field field) {
        super(field);
        checkArgument(!Strings.isNullOrEmpty(variableName));
        this.variableName = variableName;
        this.optional = optional;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ExecutionContext context) {
        Object variable = context.getSessionParameters().get(getVariableName());
        if (variable == null && !isOptional()) {
            throw new RuleExecutionException("Non optional session variable \'" + getVariableName() + "\' not available", context);
        }
        try {
            getTarget().set(context.getInstance(), variable);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuleExecutionException("Could not inject session variable \'" + getVariableName() + "\'", context, e);
        }
    }

    /**
     * Gets {@link #variableName}.
     *
     * @return {@link #variableName}
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Gets {@link #optional}.
     *
     * @return {@link #optional}
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "SessionVariableInjection [variableName=" + variableName + ", optional=" + optional + ", getTarget()=" + getTarget() + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (optional ? 1231 : 1237);
        result = prime * result + ((variableName == null) ? 0 : variableName.hashCode());
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
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SessionVariableInjection other = (SessionVariableInjection) obj;
        if (optional != other.optional) {
            return false;
        }
        if (variableName == null) {
            if (other.variableName != null) {
                return false;
            }
        } else if (!variableName.equals(other.variableName)) {
            return false;
        }
        return true;
    }

}
