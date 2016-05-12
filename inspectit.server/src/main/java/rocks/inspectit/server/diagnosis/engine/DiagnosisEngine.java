package rocks.inspectit.server.diagnosis.engine;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.inspectit.server.diagnosis.engine.session.ISessionCallback;
import rocks.inspectit.server.diagnosis.engine.session.Session;
import rocks.inspectit.server.diagnosis.engine.session.SessionPool;
import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The default implementation of {@link IDiagnosisEngine}
 *
 * @param <I> The type of input to be analyzed.
 * @param <R> The result type.
 * @author Claudio Waldvogel
 */
public class DiagnosisEngine<I, R> implements IDiagnosisEngine<I> {

    /**
     * The slf4j logger
     */
    private static Logger LOG = LoggerFactory.getLogger(DiagnosisEngine.class);

    /**
     * The {@link DiagnosisEngineConfiguration} which configured this engine instance.
     */
    private final DiagnosisEngineConfiguration<I, R> configuration;

    /**
     * The {@link SessionPool} providing {@link Session} instances to this engine instance. The configuration of the pool depends on the {@link DiagnosisEngineConfiguration}.
     */
    private final SessionPool<I, R> sessionPool;

    /**
     * The {@link ListeningExecutorService} to dispatch the diagnosis sessions.
     */
    private final ListeningExecutorService sessionExecutor;

    /**
     * Default constructor to create a new DiagnosisEngine instance.
     *
     * @param configuration The {@link DiagnosisEngineConfiguration} to be used. Must not be null.
     */
    public DiagnosisEngine(DiagnosisEngineConfiguration<I, R> configuration) {
        this.configuration = checkNotNull(configuration, "The configuration must not be null.");
        this.sessionPool = new SessionPool<>(configuration);

        ExecutorService executor = configuration.getExecutorService();
        if (executor == null) {
            executor = Executors.newFixedThreadPool(configuration.getNumSessionWorkers());
        }
        // Wrap in listing executor
        this.sessionExecutor = MoreExecutors.listeningDecorator(executor);
    }

    // -------------------------------------------------------------
    // Interface Implementation: IDiagnosisEngine
    // -------------------------------------------------------------

    @Override
    public void analyze(I input) {
        analyze(input, new SessionVariables());
    }

    @Override
    public void analyze(I input, SessionVariables variables) {
        synchronized (this) {
            final Session<I, R> session;
            try {
                session = sessionPool.borrowObject(input, variables);
            } catch (Exception e) {
                throw new DiagnosisEngineException("Failed to borrow Object from SessionPool.", e);
            }

            // Kick of the session execution
            Futures.addCallback(sessionExecutor.submit(session), new FutureCallback<R>() {

                @Override
                public void onSuccess(R result) {
                    returnSession();
                    for (ISessionCallback<R> handler : configuration.getSessionCallbacks()) {
                        //avoid calling FutureCallback#onFailure due to errors in onSuccess
                        try {
                            handler.onSuccess(result);
                        } catch (Throwable t) {
                            LOG.warn(Throwables.getStackTraceAsString(t));
                        }
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    returnSession();
                    for (ISessionCallback<R> handler : configuration.getSessionCallbacks()) {
                        try {
                            handler.onFailure(t);
                        } catch (Throwable throwable) {
                            LOG.warn(Throwables.getStackTraceAsString(throwable));
                        }
                    }
                }

                private void returnSession() {
                    try {
                        sessionPool.returnObject(session);
                    } catch (Exception e) {
                        for (ISessionCallback<R> handler : configuration.getSessionCallbacks()) {
                            handler.onFailure(new DiagnosisEngineException("Failed to return Session to ObjectPool.", e));
                        }
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(boolean awaitShutdown) throws Exception {
        synchronized (this) {
            // 1. Shutdown the ExecutorService
            if (!sessionExecutor.isShutdown()) {
                sessionExecutor.shutdown();
                if (awaitShutdown) {
                    try {
                        if (!sessionExecutor.awaitTermination(configuration.getShutdownTimeout(), TimeUnit.SECONDS)) {
                            LOG.error("DiagnosisEngine executor did not shutdown within: {} seconds.", configuration.getShutdownTimeout());
                        }
                    } catch (InterruptedException e) {
                        throw new DiagnosisEngineException("Failed to shutdown DiagnosisEngine.", e);
                    }
                }
            }
            // 2. Shutdown the session pool
            sessionPool.close();
        }
    }
}