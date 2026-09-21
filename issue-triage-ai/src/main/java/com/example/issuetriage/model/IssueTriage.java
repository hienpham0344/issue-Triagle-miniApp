package com.example.issuetriage.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IssueTriage(
	@JsonProperty("status") TriageStatus status,
	@JsonProperty("severity") Severity severity,
	@JsonProperty("component") String component,
	@JsonProperty("needs_urgent_response") boolean needsUrgentResponse,
	@JsonProperty("reason") String reason) {
}
