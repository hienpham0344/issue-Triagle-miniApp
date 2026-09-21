package com.example.issuetriage.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TriageStatus {
	@JsonProperty("classified")
	CLASSIFIED,
	@JsonProperty("insufficient_data")
	INSUFFICIENT_DATA,
	@JsonProperty("out_of_scope")
	OUT_OF_SCOPE
}
