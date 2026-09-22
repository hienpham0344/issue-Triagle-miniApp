package com.example.issuetriage.ai.gemini;

import com.example.issuetriage.ai.gemini.dto.ChatCompletionRequest;
import com.example.issuetriage.ai.gemini.dto.ChatMessage;
import com.example.issuetriage.ai.prompt.TriagePrompt;
import com.example.issuetriage.ai.schema.IssueTriageSchema;
import com.example.issuetriage.config.GeminiProperties;
import com.example.issuetriage.domain.model.IssueTriage;
import com.example.issuetriage.tool.ComponentOwnerTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GeminiRequestFactory {

	private final GeminiProperties properties;

	public GeminiRequestFactory(GeminiProperties properties) {
		this.properties = properties;
	}

	public ChatCompletionRequest createStructuredTriageRequest(String issue) {
		List<ChatMessage> messages = List.of(
				ChatMessage.system(TriagePrompt.SYSTEM_PROMPT),
				ChatMessage.user(issue)
		);

		Map<String, Object> responseFormat = Map.of(
				"type", "json_schema",
				"json_schema", Map.of(
						"name", "issue_triage",
						"strict", true,
						"schema", IssueTriageSchema.getSchema()
				)
		);

		return new ChatCompletionRequest(
				properties.model(),
				messages,
				responseFormat,
				null,
				null
		);
	}

	public ChatCompletionRequest createComponentOwnerToolRequest(IssueTriage triage) {
		List<ChatMessage> messages = List.of(
				ChatMessage.user("Lookup the owner team for software component: " + triage.component())
		);

		List<Map<String, Object>> tools = List.of(getComponentOwnerToolDefinition());

		Map<String, Object> toolChoice = Map.of(
				"type", "function",
				"function", Map.of("name", ComponentOwnerTool.TOOL_NAME)
		);

		return new ChatCompletionRequest(
				properties.model(),
				messages,
				null,
				tools,
				toolChoice
		);
	}

	public ChatCompletionRequest createFinalResponseRequest(
			String issue,
			IssueTriage triage,
			ChatMessage assistantToolCallMessage,
			ChatMessage toolResultMessage
	) {
		List<ChatMessage> messages = List.of(
				ChatMessage.system("You are an issue triage assistant. Summarize the incident concisely, " +
						"mentioning the severity, affected component, owning team, and whether urgent action is required."),
				ChatMessage.user("Issue: " + issue + "\nTriage Severity: " + triage.severity() +
						"\nComponent: " + triage.component()),
				assistantToolCallMessage,
				toolResultMessage
		);

		return new ChatCompletionRequest(
				properties.model(),
				messages,
				null,
				null,
				null
		);
	}

	private Map<String, Object> getComponentOwnerToolDefinition() {
		return Map.of(
				"type", "function",
				"function", Map.of(
						"name", ComponentOwnerTool.TOOL_NAME,
						"description", "Returns the team responsible for a software component.",
						"parameters", Map.of(
								"type", "object",
								"properties", Map.of(
										"component", Map.of(
												"type", "string",
												"description", "The software component name."
										)
								),
								"required", List.of("component"),
								"additionalProperties", false
						)
				)
		);
	}
}
