package rocks.inspectit.server.diagnosis.engine.session.result;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.session.SessionContext;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class DefaultSessionResultCollector<I> implements ISessionResultCollector<I, DefaultSessionResult<I>> {


    public DefaultSessionResultCollector() {
    }

    @Override
    public DefaultSessionResult<I> collect(SessionContext<I> context) {
        Multimap<String, ConditionFailure> conditionFailures = ArrayListMultimap.create();
        //unpack condition errors
        for (RuleOutput output : context.getStorage().getAllOutputsWithConditionErrors().values()) {
            conditionFailures.putAll(output.getRuleName(), output.getConditionFailures());
        }

        return new DefaultSessionResult<>(context.getInput(),
                conditionFailures,
                context.getStorage().mapTags(TagState.LEAF));
    }

}
