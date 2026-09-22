package com.example.issuetriage.ai.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ChatCompletionResponse(
		@JsonProperty("choices") List<Choice> choices
) {
	public record Choice(
			@JsonProperty("message") ChatMessage message
	) {}
}
