package rocks.inspectit.server.diagnosis.engine.rule.execution.store;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;

import java.util.*;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class DefaultRuleOutputStorage implements IRuleOutputStorage {

    private final SetMultimap<String, RuleOutput> allOutputs;
    private final Multimap<String, RuleOutput> conditionFailures;

    public DefaultRuleOutputStorage() {
        this.allOutputs = LinkedHashMultimap.create();
        this.conditionFailures = ArrayListMultimap.create();
    }

    @Override
    public void insert(Collection<RuleOutput> output) {
        for (RuleOutput single : output) {
            insert(single);
        }
    }

    @Override
    public void insert(RuleOutput output) {
        if (output.hasConditionFailures()) {
            conditionFailures.put(output.getEmbeddedTagType(), output);
        } else {
            allOutputs.put(output.getEmbeddedTagType(), output);
        }
    }

    @Override
    public Set<String> availableTagTypes() {
        return allOutputs.keySet();
    }

    @Override
    public Multimap<String, RuleOutput> getAllOutputsWithConditionErrors() {
        return conditionFailures;
    }

    @Override
    public Multimap<String, RuleOutput> getAllOutputs() {
        return allOutputs;
    }

    @Override
    public Multimap<String, Tag> mapTags(TagState state) {
        Multimap<String, Tag> tags = ArrayListMultimap.create();
        for (Map.Entry<String, RuleOutput> entry : allOutputs.entries()) {
            if (entry.getValue().hasResultTags()) {
                for (Tag tag : entry.getValue().getTags()) {
                    if (tag.getState().equals(state)) {
                        tags.put(tag.getType(), tag);
                    }
                }
            }
        }
        return tags;
    }

    @Override
    public Collection<RuleOutput> findLeafsByTags(Set<String> tagTypes) {
        Set<String> keys = allOutputs.keySet();
        ListIterator<String> iterator = new ArrayList<>(keys).listIterator(keys.size());
        while (iterator.hasPrevious()) {
            String previous = iterator.previous();
            if (tagTypes.contains(previous)) {
                return allOutputs.get(previous);
            }
        }
        return null;
    }

    @Override
    public void clear() {
        this.allOutputs.clear();
    }
}
