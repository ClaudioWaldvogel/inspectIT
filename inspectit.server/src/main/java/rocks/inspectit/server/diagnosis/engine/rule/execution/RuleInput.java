package rocks.inspectit.server.diagnosis.engine.rule.execution;

import java.util.Collection;

import rocks.inspectit.server.diagnosis.engine.tag.Tag;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class RuleInput {

	private final Tag root;
	private final Collection<Tag> unraveled;

	public RuleInput(Tag root, Collection<Tag> unraveled) {
		this.root = root;
		this.unraveled = unraveled;
	}

	/**
	 * Gets {@link #root}.
	 *
	 * @return {@link #root}
	 */
	public Tag getRoot() {
		return root;
	}

	/**
	 * Gets {@link #unraveled}.
	 *
	 * @return {@link #unraveled}
	 */
	public Collection<Tag> getUnraveled() {
		return unraveled;
	}
}
