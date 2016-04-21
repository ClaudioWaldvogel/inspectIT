package rocks.inspectit.server.diagnosis.engine;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import rocks.inspectit.server.diagnosis.engine.session.Session;
import rocks.inspectit.server.diagnosis.engine.session.SessionPool;
import rocks.inspectit.server.diagnosis.engine.session.result.ISessionResultHandler;

/**
 * @param <I>
 * @param <R>
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class DiagnosisEngine<I, R> implements IDiagnosisEngine<I, R> {

	private static Logger LOG = LoggerFactory.getLogger(DiagnosisEngine.class);

	private final DiagnosisEngineConfiguration<I, R> configuration;
	private final SessionPool<I, R> sessionPool;
	private final ListeningExecutorService sessionExecutor;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void analyze(I input) throws Exception {
		synchronized (this) {
			final Session<I, R> session;
			try {
				session = sessionPool.borrowObject(input);
			} catch (Exception e) {
				throw new DiangosisEngineException("Failed to borrow Object from SessionPool.", e);
			}

			// Kick of the session execution
			Futures.addCallback(sessionExecutor.submit(session), new FutureCallback<R>() {
				@Override
				public void onSuccess(R result) {
					returnSession();
					for (ISessionResultHandler<R> handler : configuration.getHandler()) {
						handler.handle(result);
					}
				}

				@Override
				public void onFailure(Throwable t) {
					returnSession();
				}

				private void returnSession() {
					try {
						sessionPool.returnObject(session);
					} catch (Exception e) {
						throw new DiangosisEngineException("Failed to return Session to ObjectPool.", e);
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
						throw new DiangosisEngineException("Failed to shutdown DiagnosisEngine.", e);
					}
				}
			}
			// 2. Shutdown the session pool
			sessionPool.close();
		}
	}
}
