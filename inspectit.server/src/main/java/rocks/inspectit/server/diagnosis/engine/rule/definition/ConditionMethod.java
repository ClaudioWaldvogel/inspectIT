package rocks.inspectit.server.diagnosis.engine.rule.definition;

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

    public ConditionMethod(String name,
                           String hint,
                           Method method) {
        this.name = checkNotNull(name);
        this.hint = hint;
        this.method = checkNotNull(method);
    }

    public ConditionFailure execute(ExecutionContext context) {
        try {
            boolean valid = (boolean) getMethod().invoke(context.getInstance());
            if (!valid) {
                //Store information about the failed condition for later usage
                return new ConditionFailure(getName(), getHint());
            }
        } catch (InvocationTargetException |
                IllegalAccessException e) {
            throw new RuleExecutionException("Failed to execute condition method", e);
        }
        return null;
    }

    //-------------------------------------------------------------
    // Methods: Accessors
    //-------------------------------------------------------------

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
    // Methods: Generated
    //-------------------------------------------------------------

    @Override
    public String toString() {
        return "ConditionMethod{" +
                "name='" + name + '\'' +
                ", hint='" + hint + '\'' +
                ", method=" + method +
                '}';
    }

}
