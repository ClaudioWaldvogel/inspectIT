package rocks.inspectit.server.diagnosis.engine.rule;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue.InjectionStrategy;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

import java.lang.reflect.Field;

import static rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue.InjectionStrategy.BY_VALUE;

/**
 * FieldInjection implementation to inject <code>Tag</code>s, respectively the value of a <code>Tag</code>.
 *
 * @author Claudio Waldvogel
 */
public class TagInjection extends FieldInjection {

	/**
	 * The type if tag to be injected.
	 */
	private final String type;

	/**
	 * The strategy how the <code>Tag</code> is injected.
	 *
	 * @see InjectionStrategy
	 */
	private final InjectionStrategy injectionStrategy;

	/**
	 * Default Constructor
	 *
	 * @param type
	 * 		The type of <code>Tag</code> to be injected
	 * @param injectee
	 * 		The injectee field
	 */
	public TagInjection(String type, Field injectee) {
		this(type, injectee, BY_VALUE);
	}

	/**
	 * Constructor to define a InjectionStrategy
	 *
	 * @param type
	 * 		The type of <code>Tag</code> to be injected
	 * @param injectee
	 * 		The injectee field
	 * @param injectionStrategy
	 * 		The <code>InjectionStrategy</code>
	 * @see InjectionStrategy
	 */
	public TagInjection(String type, Field injectee, InjectionStrategy injectionStrategy) {
		super(injectee);
		this.type = type;
		this.injectionStrategy = injectionStrategy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object determineValueToInject(ExecutionContext context) {
		//extract the required type form the unraveled list of input Tags
		for (Tag tag : context.getRuleInput().getUnraveled()) {
			if (tag.getType().equals(getType())) {
				return getInjectionStrategy().equals(BY_VALUE) ? tag.getValue() : tag;
			}
		}
		throw new RuleExecutionException("Unable to find Tag: " + getType() + " in RuleInput.", context);
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #type}.
	 *
	 * @return {@link #type}
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets {@link #injectionStrategy}.
	 *
	 * @return {@link #injectionStrategy}
	 */
	public InjectionStrategy getInjectionStrategy() {
		return injectionStrategy;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "TagInjection [type=" + type + ", injectionStrategy=" + injectionStrategy + ", getInjectee()=" + getInjectee() + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		TagInjection that = (TagInjection) o;

		return new EqualsBuilder().appendSuper(super.equals(o)).append(getType(), that.getType()).append(getInjectionStrategy(), that.getInjectionStrategy()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(getType()).append(getInjectionStrategy()).toHashCode();
	}
}
