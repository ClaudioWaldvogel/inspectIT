package rocks.inspectit.server.diagnosis.engine.rule;

import com.google.common.base.Strings;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class RuleOutput {

	private final String ruleName;
	private final String embeddedTagType;
	private final Collection<ConditionFailure> conditionFailures;
	private final Collection<Tag> tags;

	public RuleOutput(String ruleName, String embeddedTagType, Tag tag) {
		this(ruleName, embeddedTagType, Collections.<ConditionFailure> emptyList(), Collections.singleton(tag));
	}

	public RuleOutput(String ruleName, String embeddedTagType, Collection<ConditionFailure> conditionFailures, Collection<Tag> tags) {
		checkArgument(!Strings.isNullOrEmpty(ruleName), "Rule name must not be empty!");
		checkArgument(!Strings.isNullOrEmpty(embeddedTagType), "Contained tag type name must not be empty!");
		this.embeddedTagType = embeddedTagType;
		this.ruleName = ruleName;
		this.conditionFailures = checkNotNull(conditionFailures, "Collection must not be empty!");
		this.tags = checkNotNull(tags, "Collections must not be empty!");
	}

	// -------------------------------------------------------------
	// Methods: Convenience
	// -------------------------------------------------------------

	public boolean hasConditionFailures() {
		return !conditionFailures.isEmpty();
	}

	public boolean hasResultTags() {
		return !tags.isEmpty();
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #ruleName}.
	 *
	 * @return {@link #ruleName}
	 */
	public String getRuleName() {
		return ruleName;
	}

	/**
	 * Gets {@link #embeddedTagType}.
	 *
	 * @return {@link #embeddedTagType}
	 */
	public String getEmbeddedTagType() {
		return embeddedTagType;
	}

	/**
	 * Gets {@link #conditionFailures}.
	 *
	 * @return {@link #conditionFailures}
	 */
	public Collection<ConditionFailure> getConditionFailures() {
		return conditionFailures;
	}

	/**
	 * Gets {@link #tags}.
	 *
	 * @return {@link #tags}
	 */
	public Collection<Tag> getTags() {
		return tags;
	}
}
