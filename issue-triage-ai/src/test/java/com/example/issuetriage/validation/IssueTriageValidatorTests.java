package com.example.issuetriage.validation;

import com.example.issuetriage.exception.IssueTriageValidationException;
import com.example.issuetriage.model.IssueTriage;
import com.example.issuetriage.model.Severity;
import com.example.issuetriage.model.TriageStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IssueTriageValidatorTests {

	private final IssueTriageValidator validator = new IssueTriageValidator();

	@Test
	void acceptsCompleteClassifiedTriage() {
		assertThatCode(() -> validator.validate(triage(
			TriageStatus.CLASSIFIED, Severity.P1, "authentication", true, "Sign-in is unavailable.")))
			.doesNotThrowAnyException();
	}

	@Test
	void rejectsClassifiedTriageWithoutSeverity() {
		assertThatThrownBy(() -> validator.validate(triage(
			TriageStatus.CLASSIFIED, null, "authentication", false, "Sign-in is unavailable.")))
			.isInstanceOf(IssueTriageValidationException.class)
			.hasMessageContaining("severity");
	}

	@Test
	void rejectsClassifiedTriageWithBlankComponent() {
		assertThatThrownBy(() -> validator.validate(triage(
			TriageStatus.CLASSIFIED, Severity.P1, "  ", false, "Sign-in is unavailable.")))
			.isInstanceOf(IssueTriageValidationException.class)
			.hasMessageContaining("component");
	}

	@Test
	void rejectsClassifiedTriageWithBlankReason() {
		assertThatThrownBy(() -> validator.validate(triage(
			TriageStatus.CLASSIFIED, Severity.P1, "authentication", false, "  ")))
			.isInstanceOf(IssueTriageValidationException.class)
			.hasMessageContaining("reason");
	}

	@Test
	void acceptsInsufficientDataTriageWithoutSeverity() {
		assertThatCode(() -> validator.validate(triage(
			TriageStatus.INSUFFICIENT_DATA, null, null, false, "More detail is needed.")))
			.doesNotThrowAnyException();
	}

	@Test
	void rejectsInsufficientDataTriageWithSeverity() {
		assertThatThrownBy(() -> validator.validate(triage(
			TriageStatus.INSUFFICIENT_DATA, Severity.P1, null, false, "More detail is needed.")))
			.isInstanceOf(IssueTriageValidationException.class)
			.hasMessageContaining("severity");
	}

	@Test
	void rejectsOutOfScopeTriageNeedingUrgentResponse() {
		assertThatThrownBy(() -> validator.validate(triage(
			TriageStatus.OUT_OF_SCOPE, null, null, true, "This is not a product issue.")))
			.isInstanceOf(IssueTriageValidationException.class)
			.hasMessageContaining("urgent");
	}

	private IssueTriage triage(
		TriageStatus status, Severity severity, String component, boolean needsUrgentResponse, String reason) {
		return new IssueTriage(status, severity, component, needsUrgentResponse, reason);
	}
}
