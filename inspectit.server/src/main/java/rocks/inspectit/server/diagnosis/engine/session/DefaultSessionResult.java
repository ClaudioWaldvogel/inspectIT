package rocks.inspectit.server.diagnosis.engine.session;

import com.google.common.collect.Multimap;

import rocks.inspectit.server.diagnosis.engine.rule.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class DefaultSessionResult<I> {

	private final I input;

	private final Multimap<String, ConditionFailure> conditionFailures;
	private final Multimap<String, Tag> endTags;

	public DefaultSessionResult(I input, Multimap<String, ConditionFailure> conditionFailures, Multimap<String, Tag> endTags) {
		this.input = input;
		this.conditionFailures = conditionFailures;
		this.endTags = endTags;
	}

	// -------------------------------------------------------------
	// Methods: accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #input}.
	 *
	 * @return {@link #input}
	 */
	public I getInput() {
		return input;
	}

	/**
	 * Gets {@link #conditionFailures}.
	 *
	 * @return {@link #conditionFailures}
	 */
	public Multimap<String, ConditionFailure> getConditionFailures() {
		return conditionFailures;
	}

	/**
	 * Gets {@link #endTags}.
	 *
	 * @return {@link #endTags}
	 */
	public Multimap<String, Tag> getEndTags() {
		return endTags;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	@Override
	public String toString() {
		return "DefaultSessionResult{" + "input=" + input + ", conditionFailures=" + conditionFailures + ", endTags=" + endTags + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DefaultSessionResult<?> that = (DefaultSessionResult<?>) o;

		if (getInput() != null ? !getInput().equals(that.getInput()) : that.getInput() != null) {
			return false;
		}
		if (getConditionFailures() != null ? !getConditionFailures().equals(that.getConditionFailures()) : that.getConditionFailures() != null) {
			return false;
		}
		return getEndTags() != null ? getEndTags().equals(that.getEndTags()) : that.getEndTags() == null;

	}

	@Override
	public int hashCode() {
		int result = getInput() != null ? getInput().hashCode() : 0;
		result = 31 * result + (getConditionFailures() != null ? getConditionFailures().hashCode() : 0);
		result = 31 * result + (getEndTags() != null ? getEndTags().hashCode() : 0);
		return result;
	}
}
