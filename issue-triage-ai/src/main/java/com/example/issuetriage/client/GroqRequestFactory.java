package com.example.issuetriage.client;

import com.example.issuetriage.config.GroqProperties;
import com.example.issuetriage.dto.groq.ChatCompletionRequest;
import com.example.issuetriage.dto.groq.ChatCompletionResponse;
import com.example.issuetriage.dto.groq.ChatMessage;
import com.example.issuetriage.model.IssueTriage;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Component
public class GroqRequestFactory {

	private static final String SYSTEM_PROMPT = "Classify the reported issue. Return only the requested JSON object.";

	private final GroqProperties properties;
	private final ObjectMapper mapper;

	public GroqRequestFactory(GroqProperties properties, ObjectMapper mapper) {
		this.properties = properties;
		this.mapper = mapper;
	}

	public ChatCompletionRequest structuredTriage(String issue) {
		return new ChatCompletionRequest(
			properties.model(),
			List.of(new ChatMessage("system", SYSTEM_PROMPT), new ChatMessage("user", issue)),
			Map.of("type", "json_schema", "json_schema", Map.of(
				"name", "issue_triage",
				"strict", true,
				"schema", issueTriageSchema())));
	}

	public IssueTriage readIssueTriage(JsonNode groqResponse) {
		try {
			ChatCompletionResponse response = mapper.treeToValue(groqResponse, ChatCompletionResponse.class);
			return mapper.readValue(response.choices().getFirst().message().content(), IssueTriage.class);
		} catch (JacksonException exception) {
			throw new IllegalArgumentException("Groq response did not contain a valid issue triage", exception);
		}
	}

	private Map<String, Object> issueTriageSchema() {
		return Map.of(
			"type", "object",
			"additionalProperties", false,
			"required", List.of("status", "severity", "component", "needs_urgent_response", "reason"),
			"properties", Map.of(
				"status", Map.of("type", "string", "enum", List.of("classified", "insufficient_data", "out_of_scope")),
				"severity", Map.of("type", List.of("string", "null"), "enum", Arrays.asList("P0", "P1", "P2", "P3", null)),
				"component", Map.of("type", List.of("string", "null")),
				"needs_urgent_response", Map.of("type", "boolean"),
				"reason", Map.of("type", "string")));
	}
}
