package rocks.inspectit.server.diagnosis.engine.rule.store;

import com.google.common.collect.Multimap;
import rocks.inspectit.server.diagnosis.engine.rule.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;

import java.util.Collection;
import java.util.Set;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public interface IRuleOutputStorage {

    void insert(Collection<RuleOutput> output);

    void insert(RuleOutput output);

    Set<String> availableTagTypes();

    Collection<RuleOutput> findLeafsByTags(Set<String> requestedTypes);

    Multimap<String, RuleOutput> getAllOutputsWithConditionErrors();

    Multimap<String, RuleOutput> getAllOutputs();

    Multimap<String, Tag> mapTags(TagState state);

    void clear();
}
