package rocks.inspectit.server.diagnosis.engine.rule;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue.InjectionStrategy;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

import java.lang.reflect.Field;

import static rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue.InjectionStrategy.BY_VALUE;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class TagInjection extends FieldInjection {

    private final String tagType;
    private final InjectionStrategy injectionStrategy;

    public TagInjection(String tagType, Field target) {
        this(tagType, target, BY_VALUE);
    }

    public TagInjection(String tagType, Field target, InjectionStrategy injectionStrategy) {
        super(target);
        this.tagType = tagType;
        this.injectionStrategy = injectionStrategy;
    }

    @Override
    public void execute(ExecutionContext context) {
        for (Tag tag : context.getRuleInput().getUnraveled()) {
            if (tag.getType().equals(getTagType())) {
                // Ensure field is accessible
                getTarget().setAccessible(true);
                // ensure proper type injection
                Object toInject;
                switch (getInjectionStrategy()) {
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
     * Gets {@link #injectionStrategy}.
     *
     * @return {@link #injectionStrategy}
     */
    public InjectionStrategy getInjectionStrategy() {
        return injectionStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TagInjection [type=" + tagType + ", injectionStrategy=" + injectionStrategy + ", getTarget()=" + getTarget() + "]";
    }

    // -------------------------------------------------------------
    // Methods: Generated
    // -------------------------------------------------------------

}
