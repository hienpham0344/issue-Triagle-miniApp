package com.example.issuetriage.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ComponentOwnerResult(
	@JsonProperty("component") String component,
	@JsonProperty("owner") String owner
) {}
