package rocks.inspectit.server.diagnosis.engine.tag;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class Tag {

	private final String type;
	private final Object value;
	private final Tag parent;

	private TagState state = TagState.LEAF;

	public Tag(String type) {
		this(type, null, null);
	}

	public Tag(String type, Object value) {
		this(type, value, null);
	}

	public Tag(String type, Object value, Tag parent) {
		this.value = value;
		this.type = type;
		this.parent = parent;
		if (this.parent != null) {
			this.parent.markParent();
		}
	}


	/**
	 * Gets {@link #type}.
	 *
	 * @return {@link #type}
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets {@link #value}.
	 *
	 * @return {@link #value}
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Gets {@link #parent}.
	 *
	 * @return {@link #parent}
	 */
	public Tag getParent() {
		return parent;
	}

	/**
	 * Gets {@link #state}.
	 *
	 * @return {@link #state}
	 */
	public TagState getState() {
		return state;
	}

	private synchronized void markParent() {
		if (state.equals(TagState.LEAF)) {
			state = TagState.PARENT;
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("type", type).append("value", value).append("parent", parent).append("state", state).toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		Tag tag = (Tag) o;

		return new EqualsBuilder().append(getType(), tag.getType()).append(getValue(), tag.getValue()).append(getParent(), tag.getParent()).append(getState(), tag.getState()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getType()).append(getValue()).append(getParent()).append(getState()).toHashCode();
	}
}
