package com.example.issuetriage.ai.gemini;

import com.example.issuetriage.ai.gemini.dto.ChatCompletionResponse;
import com.example.issuetriage.ai.gemini.dto.ChatMessage;
import com.example.issuetriage.ai.gemini.dto.ToolCall;
import com.example.issuetriage.domain.model.IssueTriage;
import com.example.issuetriage.exception.LlmApiException;
import com.example.issuetriage.exception.ToolExecutionException;
import com.example.issuetriage.tool.ComponentOwnerArguments;
import com.example.issuetriage.tool.ComponentOwnerResult;
import com.example.issuetriage.tool.ComponentOwnerTool;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class GeminiResponseParser {

	private final ObjectMapper objectMapper;

	public GeminiResponseParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public IssueTriage parseStructuredTriage(ChatCompletionResponse response) {
		ChatMessage message = extractFirstMessage(response);
		String content = message.content();
		if (content == null || content.isBlank()) {
			throw new LlmApiException(500, "Gemini structured output response was empty");
		}

		try {
			return objectMapper.readValue(content, IssueTriage.class);
		} catch (JacksonException e) {
			throw new LlmApiException(500, "Failed to parse structured triage JSON from response: " + e.getMessage());
		}
	}

	public ChatMessage extractAssistantMessage(ChatCompletionResponse response) {
		return extractFirstMessage(response);
	}

	public ToolCall extractToolCall(ChatMessage message) {
		if (message == null) {
			throw new ToolExecutionException("Assistant message is null");
		}

		List<ToolCall> toolCalls = message.toolCalls();
		if (toolCalls == null || toolCalls.isEmpty()) {
			throw new ToolExecutionException("Gemini response did not contain any tool calls");
		}

		ToolCall toolCall = toolCalls.getFirst();
		if (toolCall == null || toolCall.function() == null) {
			throw new ToolExecutionException("Malformed tool call structure in Gemini response");
		}

		if (!ComponentOwnerTool.TOOL_NAME.equals(toolCall.function().name())) {
			throw new ToolExecutionException("Unsupported tool requested by Gemini: " + toolCall.function().name());
		}

		return toolCall;
	}

	public ToolCall extractToolCall(ChatCompletionResponse response) {
		return extractToolCall(extractFirstMessage(response));
	}

	public ComponentOwnerArguments parseComponentOwnerArguments(ToolCall toolCall) {
		if (toolCall == null || toolCall.function() == null) {
			throw new ToolExecutionException("ToolCall or function is null");
		}

		String argsJson = toolCall.function().arguments();
		if (argsJson == null || argsJson.isBlank()) {
			throw new ToolExecutionException("Tool call function arguments are missing or blank");
		}

		ComponentOwnerArguments arguments;
		try {
			arguments = objectMapper.readValue(argsJson, ComponentOwnerArguments.class);
		} catch (JacksonException e) {
			throw new ToolExecutionException("Failed to deserialize tool arguments JSON: " + e.getMessage(), e);
		}

		if (arguments == null || arguments.component() == null || arguments.component().isBlank()) {
			throw new ToolExecutionException("Component argument is missing or blank");
		}

		return arguments;
	}

	public String serializeToolResult(ComponentOwnerResult result) {
		try {
			return objectMapper.writeValueAsString(result);
		} catch (JacksonException e) {
			throw new ToolExecutionException("Failed to serialize tool result to JSON: " + e.getMessage(), e);
		}
	}

	public String extractFinalMessage(ChatCompletionResponse response) {
		ChatMessage message = extractFirstMessage(response);
		String content = message.content();
		if (content == null || content.isBlank()) {
			throw new LlmApiException(500, "Gemini final response message was empty");
		}
		return content.trim();
	}

	private ChatMessage extractFirstMessage(ChatCompletionResponse response) {
		if (response == null || response.choices() == null || response.choices().isEmpty()) {
			throw new LlmApiException(500, "Gemini returned no choices in chat completion response");
		}
		ChatMessage message = response.choices().getFirst().message();
		if (message == null) {
			throw new LlmApiException(500, "Gemini choice message was null");
		}
		return message;
	}
}
