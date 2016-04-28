package rocks.inspectit.server.diagnosis.engine.rule.definition;

import rocks.inspectit.server.diagnosis.engine.rule.api.InjectionType;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

import java.lang.reflect.Field;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class TagInjection extends FieldInjection {

    private final String tagType;
    private final InjectionType injectionType;

    public TagInjection(String tagType, Field target) {
        this(tagType, target, InjectionType.BY_VALUE);
    }

    public TagInjection(String tagType, Field target, InjectionType injectionType) {
        super(target);
        this.tagType = tagType;
        this.injectionType = injectionType;
    }

    @Override
    public void execute(ExecutionContext context) {
        for (Tag tag : context.getRuleInput().getUnraveled()) {
            if (tag.getType().equals(tagType)) {
                // Ensure field is accessible
                getTarget().setAccessible(true);
                // ensure proper type injection
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
                    throw new RuleExecutionException("Failed to inject tag value (s).", context, e);
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Methods: Accessors
    // -------------------------------------------------------------

    /**
     * Gets {@link #tagType}.
     *
     * @return {@link #tagType}
     */
    public String getTagType() {
        return tagType;
    }

    /**
     * Gets {@link #injectionType}.
     *
     * @return {@link #injectionType}
     */
    public InjectionType getInjectionType() {
        return injectionType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TagInjection [tagType=" + tagType + ", injectionType=" + injectionType + ", getTarget()=" + getTarget() + "]";
    }

    // -------------------------------------------------------------
    // Methods: Generated
    // -------------------------------------------------------------

}
