package com.example.issuetriage.ai.gemini.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatCompletionRequest(
		@JsonProperty("model") String model,
		@JsonProperty("messages") List<ChatMessage> messages,
		@JsonProperty("response_format") Object responseFormat,
		@JsonProperty("tools") List<Map<String, Object>> tools,
		@JsonProperty("tool_choice") Object toolChoice
) {}
