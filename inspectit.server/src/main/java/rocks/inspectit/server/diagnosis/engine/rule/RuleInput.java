package rocks.inspectit.server.diagnosis.engine.rule;

import rocks.inspectit.server.diagnosis.engine.tag.Tag;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class RuleInput {

	private final Tag root;
	private final Collection<Tag> unraveled;

	public RuleInput(Tag root) {
		this(root, Collections.singleton(root));
	}

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
