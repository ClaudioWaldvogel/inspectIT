package rocks.inspectit.server.diagnosis.engine.tag;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class Tags {

    public static final String ROOT_TAG = "Root";

    private Tags() {
        // Must not be instantiated
    }

    public static Tag rootTag(Object input) {
        return tag(ROOT_TAG, input);
    }

    public static Tag tag(String type, Object input) {
        return tag(type, input, null);
    }

    public static Tag tag(String type, Object input, Tag parent) {
        return new Tag(type, input, parent);
    }

    public static Collection<Tag> tags(String type, Tag parent, Object... inputs) {
        return tags(type, parent, Arrays.asList(inputs));
    }

    public static Collection<Tag> tags(String type, Tag parent, Collection<Object> inputs) {
        Collection<Tag> result = new ArrayList<>();
        for (Object input : inputs) {
            result.add(tag(type, input, parent));
        }
        return result;
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
