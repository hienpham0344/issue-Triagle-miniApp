package com.example.issuetriage.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ComponentOwnerArguments(
	@JsonProperty("component") String component
) {}
