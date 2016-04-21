package rocks.inspectit.server.diagnosis.engine.rule.definition;

import rocks.inspectit.server.diagnosis.engine.rule.api.InjectionType;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

import java.lang.reflect.Field;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class TagInjection {

    private String tagType;
    private Field target;
    private InjectionType injectionType;

    public TagInjection(String tagType,
                        Field target,
                        InjectionType injectionType) {
        this.tagType = tagType;
        this.target = target;
        this.injectionType = injectionType;
    }

    public void execute(ExecutionContext context) {
        for (Tag tag : context.getRuleInput().getUnraveled()) {
            if (tag.getType().equals(tagType)) {
                //simply make accessible. No need to restore accessibility since this instance is used only once
                getTarget().setAccessible(true);
                //ensure proper type injection
                Object toInject;
                switch (getInjectionType()) {
                    case BY_TAG:
                        toInject = tag;
                        break;
                    case BY_VALUE:
                    default:
                        toInject = tag.getValue();
                }
                try {
                    getTarget().set(context.getInstance(), toInject);
                } catch (IllegalAccessException e) {
                    throw new RuleExecutionException("Failed to inject required tag value.", e);
                }
            }
        }
    }

    //-------------------------------------------------------------
    // Methods: Accessors
    //-------------------------------------------------------------

    /**
     * Gets {@link #tagType}.
     *
     * @return {@link #tagType}
     */
    public String getTagType() {
        return tagType;
    }

    /**
     * Gets {@link #target}.
     *
     * @return {@link #target}
     */
    public Field getTarget() {
        return target;
    }

    /**
     * Gets {@link #injectionType}.
     *
     * @return {@link #injectionType}
     */
    public InjectionType getInjectionType() {
        return injectionType;
    }

    //-------------------------------------------------------------
    // Methods: Generated
    //-------------------------------------------------------------

    @Override
    public String toString() {
        return "TagInjection{" +
                "tagType='" + tagType + '\'' +
                ", target=" + target +
                ", injectionType=" + injectionType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagInjection that = (TagInjection) o;

        if (getTagType() != null ? !getTagType().equals(that.getTagType()) : that.getTagType() != null) return false;
        if (getTarget() != null ? !getTarget().equals(that.getTarget()) : that.getTarget() != null) return false;
        return getInjectionType() == that.getInjectionType();

    }

    @Override
    public int hashCode() {
        int result = getTagType() != null ? getTagType().hashCode() : 0;
        result = 31 * result + (getTarget() != null ? getTarget().hashCode() : 0);
        result = 31 * result + (getInjectionType() != null ? getInjectionType().hashCode() : 0);
        return result;
    }

}
