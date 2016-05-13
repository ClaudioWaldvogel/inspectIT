package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;

/**
 * Value object providing information to execute a rule implementation. A instance of
 * <code>ExecutionContext</code> is valid for exactly one execution of a rule implementation.
 *
 * <pre>
 *     This encloses:
 * <ul>
 *     <li>All <code>TagInjection</code></li>
 *     <li>All <code>SessionVariables</code></li>
 *     <li>All <code>ConditionMethod</code></li>
 *     <li>The <code>ActionMethod</code></li>
 * </ul>
 * </pre>
 *
 * After the rule is executed the ExecutionContext is invalid and destroyed.
 *
 * @author Claudio Waldvogel
 */
public class ExecutionContext {

	/**
	 * The backing rule implementation.
	 */
	private final Object instance;

	/**
	 * The <code>RuleDefinition</code> to be executed. The <code>RuleDefinition</code> is a
	 * abstracted and generalized of a rule implementation {@link #instance)}.
	 *
	 * @see RuleDefinition
	 */
	private final RuleDefinition definition;

	/**
	 * The input to be processed by the rule.
	 *
	 * @see RuleInput
	 */
	private final RuleInput input;

	/**
	 * TODO Check if SessionVariables should be nested in RuleInput
	 * <p>
	 * Container providing all the session variables.
	 *
	 * @see SessionVariables
	 */
	private final SessionVariables sessionParameters;

	/**
	 * Default Constructor.
	 *
	 * @param definition
	 *            The <code>RuleDefinition</code>
	 * @param instance
	 *            The <code>actual implementation</code>
	 * @param input
	 *            The <code>RuleInput</code> to be processed
	 */
	public ExecutionContext(RuleDefinition definition, Object instance, RuleInput input) {
		this(definition, instance, input, new SessionVariables());
	}

	/**
	 * Constructor with {@link SessionVariables}.
	 *
	 * @param definition
	 *            The <code>RuleDefinition</code>
	 * @param instance
	 *            The <code>actual implementation</code>
	 * @param input
	 *            The <code>RuleInput</code> to be processed
	 * @param sessionParameters
	 *            The <code>SessionVariables</code>
	 */
	public ExecutionContext(RuleDefinition definition, Object instance, RuleInput input, SessionVariables sessionParameters) {
		this.definition = checkNotNull(definition);
		this.instance = checkNotNull(instance);
		this.input = checkNotNull(input);
		this.sessionParameters = checkNotNull(sessionParameters);
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #instance)}.
	 *
	 * @return {@link #instance}
	 */
	public Object getInstance() {
		return instance;
	}

	/**
	 * Gets {@link #definition}.
	 *
	 * @return {@link #definition}
	 */
	public RuleDefinition getDefinition() {
		return definition;
	}

	/**
	 * Gets {@link #input}.
	 *
	 * @return {@link #input}
	 */
	public RuleInput getRuleInput() {
		return input;
	}

	/**
	 * Gets {@link #sessionParameters}.
	 *
	 * @return {@link #sessionParameters}
	 */
	public SessionVariables getSessionParameters() {
		return sessionParameters;
	}
}
