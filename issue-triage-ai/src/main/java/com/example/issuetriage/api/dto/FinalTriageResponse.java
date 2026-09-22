package com.example.issuetriage.api.dto;

import com.example.issuetriage.domain.model.IssueTriage;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record FinalTriageResponse(
		IssueTriage triage,
		String componentOwner,
		String finalMessage
) {

	public static FinalTriageResponse withoutTool(IssueTriage triage) {
		return new FinalTriageResponse(triage, null, null);
	}
}
