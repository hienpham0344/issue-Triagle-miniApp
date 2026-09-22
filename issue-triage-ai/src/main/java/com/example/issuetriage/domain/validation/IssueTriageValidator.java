package com.example.issuetriage.domain.validation;

import com.example.issuetriage.domain.model.IssueTriage;
import com.example.issuetriage.exception.IssueTriageValidationException;
import org.springframework.stereotype.Component;

@Component
public class IssueTriageValidator {

	public void validate(IssueTriage triage) {
		if (triage == null) {
			throw new IssueTriageValidationException("Issue triage must not be null");
		}
		if (triage.status() == null) {
			throw new IssueTriageValidationException("Issue triage status must not be null");
		}

		switch (triage.status()) {
			case CLASSIFIED -> validateClassified(triage);
			case INSUFFICIENT_DATA, OUT_OF_SCOPE -> validateNonClassified(triage);
		}
	}

	private void validateClassified(IssueTriage triage) {
		if (triage.severity() == null) {
			throw new IssueTriageValidationException("Classified issue triage requires a severity");
		}
		if (isBlank(triage.component())) {
			throw new IssueTriageValidationException("Classified issue triage requires a non-blank component");
		}
		if (isBlank(triage.reason())) {
			throw new IssueTriageValidationException("Classified issue triage requires a non-blank reason");
		}
	}

	private void validateNonClassified(IssueTriage triage) {
		if (triage.severity() != null) {
			throw new IssueTriageValidationException("Non-classified issue triage must not include a severity");
		}
		if (triage.needsUrgentResponse()) {
			throw new IssueTriageValidationException("Non-classified issue triage must not need an urgent response");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
