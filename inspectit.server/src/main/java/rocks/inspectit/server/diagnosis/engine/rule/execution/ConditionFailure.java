package rocks.inspectit.server.diagnosis.engine.rule.execution;

import com.google.common.base.Objects;

/**
 *
 *
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class ConditionFailure {

	private final String conditionName;
	private final String hint;

	/**
	 *
	 * @param conditionName
	 * @param hint
	 */
	public ConditionFailure(String conditionName, String hint) {
		this.conditionName = conditionName;
		this.hint = hint;
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #conditionName}.
	 *
	 * @return {@link #conditionName}
	 */
	public String getConditionName() {
		return conditionName;
	}

	/**
	 * Gets {@link #hint}.
	 *
	 * @return {@link #hint}
	 */
	public String getHint() {
		return hint;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	@Override
	public String toString() {
		return "ConditionFailure{" + "conditionName='" + conditionName + '\'' + ", hint='" + hint + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ConditionFailure that = (ConditionFailure) o;
		return Objects.equal(getConditionName(), that.getConditionName()) && Objects.equal(getHint(), that.getHint());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getConditionName(), getHint());
	}
}
