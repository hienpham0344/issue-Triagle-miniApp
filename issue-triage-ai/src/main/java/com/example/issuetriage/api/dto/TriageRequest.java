package com.example.issuetriage.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TriageRequest(
		@NotBlank(message = "Issue description must not be blank")
		String issue
) {}
