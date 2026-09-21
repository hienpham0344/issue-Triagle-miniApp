package com.example.issuetriage.dto.groq;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ChatCompletionRequest(
	String model,
	List<ChatMessage> messages,
	@JsonProperty("response_format") Map<String, Object> responseFormat) {
}
