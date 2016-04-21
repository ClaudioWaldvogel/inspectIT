package rocks.inspectit.server.diagnosis.engine.tag;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class Tags {

	public static final String ROOT_TAG = "Root";

	private Tags() {
		// Must not be instantiated
	}

	public static Tag rootTag(Object input) {
		return new Tag(ROOT_TAG, input);
	}

	public static Collection<Tag> unwrap(Tag leaf, Collection<String> parents) {
		// Insert Leaf itself to all tags list
		Set<Tag> tags = Sets.newHashSet(leaf);
		// We must not continue, if only 1 tag type is required. This is already available
		if (parents.size() > 1) {
			Tag parent = leaf.getParent();
			// Start stepping up the hierarchy chain to collect all required tags.
			// Our contract is that all inputs to a Rule must be on the same path, starting at the
			// leaf tag
			while (parent != null) {
				if (parents.contains(parent.getType())) {
					tags.add(parent);
				}
				parent = parent.getParent();
			}
		}
		return tags;
	}

}
