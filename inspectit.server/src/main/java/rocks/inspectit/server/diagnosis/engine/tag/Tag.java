package rocks.inspectit.server.diagnosis.engine.tag;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Objects;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class Tag {

	private static final AtomicInteger IDS = new AtomicInteger();
	private final int id;
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
		this.id = IDS.incrementAndGet();
		this.value = value;
		this.type = type;
		this.parent = parent;
		if (this.parent != null) {
			this.parent.markParent();
		}
	}

	/**
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public int getId() {
		return id;
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
		return "Tag{" + "id=" + id + ", type='" + type + '\'' + ", value=" + value + ", parent=" + parent + ", state=" + state + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Tag tag = (Tag) o;
		return getId() == tag.getId() && Objects.equal(getType(), tag.getType()) && Objects.equal(getValue(), tag.getValue()) && Objects.equal(getParent(), tag.getParent());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId(), getType(), getValue(), getParent());
	}

}
