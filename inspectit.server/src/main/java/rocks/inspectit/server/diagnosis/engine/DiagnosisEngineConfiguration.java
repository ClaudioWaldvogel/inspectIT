package rocks.inspectit.server.diagnosis.engine;

import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.rule.store.IRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.ISessionCallback;
import rocks.inspectit.server.diagnosis.engine.session.ISessionResultCollector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class DiagnosisEngineConfiguration<I, R> {

    private int numSessionWorkers = 2;
    private int numRuleWorkers = 2;
    //in Seconds
    private int shutdownTimeout = 2;
    private Set<RuleDefinition> ruleDefinitions;
    private ExecutorService executorService;
    private Class<? extends IRuleOutputStorage> storageClass = DefaultRuleOutputStorage.class;
    private List<ISessionCallback<R>> callbacks;

    //TODO instance vs. class
    private ISessionResultCollector<I, R> resultCollector;

    public DiagnosisEngineConfiguration() {
        ruleDefinitions = new HashSet<>();
        callbacks = new ArrayList<>();
    }

    public DiagnosisEngineConfiguration<I, R> setNumSessionWorkers(int numSessionWorkers) {
        checkArgument(numSessionWorkers > 0, "numSessionWorkers must be at least 1.");
        this.numSessionWorkers = numSessionWorkers;
        return this;
    }

    public DiagnosisEngineConfiguration<I, R> setExecutorService(ExecutorService executorService) {
        this.executorService = checkNotNull(executorService, "The ExecutorService must not be null.");
        return this;
    }

    public DiagnosisEngineConfiguration<I, R> setRuleClasses(Set<Class<?>> rulesClasses) {
        this.ruleDefinitions.addAll(Rules.define(checkNotNull(rulesClasses, "Set of rule classes must not be null.")));
        return this;
    }

    public DiagnosisEngineConfiguration<I, R> setRuleClass(Class<?> rulesClass) {
        this.ruleDefinitions.add(Rules.define(checkNotNull(rulesClass, "Rule classes must not be null.")));
        return this;
    }

    public DiagnosisEngineConfiguration<I, R> setNumRuleWorkers(int numRuleWorkers) {
        this.numRuleWorkers = numRuleWorkers;
        return this;
    }

    public DiagnosisEngineConfiguration<I, R> setShutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
        return this;
    }

    public DiagnosisEngineConfiguration<I, R> setStorageClass(Class<? extends IRuleOutputStorage> storageClass) {
        this.storageClass = storageClass;
        return this;
    }

    public DiagnosisEngineConfiguration<I, R> setResultCollector(ISessionResultCollector<I, R> resultCollector) {
        this.resultCollector = resultCollector;
        return this;
    }

    public DiagnosisEngineConfiguration<I, R> setSessionCallback(ISessionCallback<R> listener) {
        this.callbacks.add(listener);
        return this;
    }

    public DiagnosisEngineConfiguration<I, R> setSessionCallbacks(List<ISessionCallback<R>> handler) {
        this.callbacks.addAll(handler);
        return this;
    }

    /**
     * Gets {@link #numSessionWorkers}.
     *
     * @return {@link #numSessionWorkers}
     */
    public int getNumSessionWorkers() {
        return numSessionWorkers;
    }

    /**
     * Gets {@link #numRuleWorkers}.
     *
     * @return {@link #numRuleWorkers}
     */
    public int getNumRuleWorkers() {
        return numRuleWorkers;
    }

    /**
     * Gets {@link #shutdownTimeout}.
     *
     * @return {@link #shutdownTimeout}
     */
    public int getShutdownTimeout() {
        return shutdownTimeout;
    }

    /**
     * Gets {@link #ruleDefinitions}.
     *
     * @return {@link #ruleDefinitions}
     */
    public Set<RuleDefinition> getRuleDefinitions() {
        return ruleDefinitions;
    }

    /**
     * Gets {@link #executorService}.
     *
     * @return {@link #executorService}
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Gets {@link #storageClass}.
     *
     * @return {@link #storageClass}
     */
    public Class<? extends IRuleOutputStorage> getStorageClass() {
        return storageClass;
    }

    /**
     * Gets {@link #resultCollector}.
     *
     * @return {@link #resultCollector}
     */
    public ISessionResultCollector<I, R> getResultCollector() {
        return resultCollector;
    }

    /**
     * Gets {@link #callbacks}.
     *
     * @return {@link #callbacks}
     */
    public List<ISessionCallback<R>> getSessionCallbacks() {
        return callbacks;
    }
}
