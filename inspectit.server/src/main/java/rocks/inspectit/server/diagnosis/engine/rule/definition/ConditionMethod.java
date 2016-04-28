package rocks.inspectit.server.diagnosis.engine.rule.definition;

import com.google.common.base.Strings;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class ConditionMethod {

    private final String name;
    private final String hint;
    private final Method method;

    public ConditionMethod(String name, String hint, Method method) {
        this.method = checkNotNull(method);
        // fix name if not provided
        this.name = Strings.isNullOrEmpty(name) ? this.method.getName() : name;
        this.hint = hint;
    }

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
