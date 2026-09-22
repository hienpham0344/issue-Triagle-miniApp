package com.example.issuetriage.application;

import com.example.issuetriage.ai.LlmClient;
import com.example.issuetriage.ai.gemini.GeminiRequestFactory;
import com.example.issuetriage.ai.gemini.GeminiResponseParser;
import com.example.issuetriage.ai.gemini.dto.ChatCompletionRequest;
import com.example.issuetriage.ai.gemini.dto.ChatCompletionResponse;
import com.example.issuetriage.ai.gemini.dto.ChatMessage;
import com.example.issuetriage.ai.gemini.dto.ToolCall;
import com.example.issuetriage.api.dto.FinalTriageResponse;
import com.example.issuetriage.domain.model.IssueTriage;
import com.example.issuetriage.domain.model.TriageStatus;
import com.example.issuetriage.domain.validation.IssueTriageValidator;
import com.example.issuetriage.tool.ComponentOwnerArguments;
import com.example.issuetriage.tool.ComponentOwnerResult;
import com.example.issuetriage.tool.ComponentOwnerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IssueTriageServiceImpl implements IssueTriageService {

	private static final Logger log = LoggerFactory.getLogger(IssueTriageServiceImpl.class);

	private final LlmClient llmClient;
	private final GeminiRequestFactory requestFactory;
	private final GeminiResponseParser responseParser;
	private final IssueTriageValidator validator;
	private final ComponentOwnerTool componentOwnerTool;

	public IssueTriageServiceImpl(
			LlmClient llmClient,
			GeminiRequestFactory requestFactory,
			GeminiResponseParser responseParser,
			IssueTriageValidator validator,
			ComponentOwnerTool componentOwnerTool
	) {
		this.llmClient = llmClient;
		this.requestFactory = requestFactory;
		this.responseParser = responseParser;
		this.validator = validator;
		this.componentOwnerTool = componentOwnerTool;
	}

	@Override
	public FinalTriageResponse triage(String issue) {

		IssueTriage triage = classifyIssue(issue);
		validator.validate(triage);

		if (!requiresComponentOwnerLookup(triage)) {
			return FinalTriageResponse.withoutTool(triage);
		}

		ChatMessage assistantToolCallMessage = requestComponentOwnerToolCall(triage);
		ToolCall toolCall = responseParser.extractToolCall(assistantToolCallMessage);
		log.info("TOOL_CALL id={} name={}", toolCall.id(), toolCall.function().name());

		ComponentOwnerResult toolResult = executeComponentOwnerTool(toolCall);

		String finalMessage = requestFinalResponse(issue, triage, assistantToolCallMessage, toolCall, toolResult);

		return new FinalTriageResponse(
				triage,
				toolResult.owner(),
				finalMessage
		);
	}

	private IssueTriage classifyIssue(String issue) {
		ChatCompletionRequest request = requestFactory.createStructuredTriageRequest(issue);
		ChatCompletionResponse response = llmClient.complete(request);
		IssueTriage triage = responseParser.parseStructuredTriage(response);
		log.info("STRUCTURED_OUTPUT status={} severity={} component={}",
				triage.status(), triage.severity(), triage.component());
		return triage;
	}

	private boolean requiresComponentOwnerLookup(IssueTriage triage) {
		return triage.status() == TriageStatus.CLASSIFIED
				&& triage.component() != null
				&& !triage.component().isBlank();
	}

	private ChatMessage requestComponentOwnerToolCall(IssueTriage triage) {
		ChatCompletionRequest request = requestFactory.createComponentOwnerToolRequest(triage);
		ChatCompletionResponse response = llmClient.complete(request);
		return responseParser.extractAssistantMessage(response);
	}

	private ComponentOwnerResult executeComponentOwnerTool(ToolCall toolCall) {
		ComponentOwnerArguments arguments = responseParser.parseComponentOwnerArguments(toolCall);
		log.info("TOOL_EXECUTION component={}", arguments.component());
		ComponentOwnerResult result = componentOwnerTool.execute(arguments);
		log.info("TOOL_RESULT component={} owner={}", result.component(), result.owner());
		return result;
	}

	private String requestFinalResponse(
			String issue,
			IssueTriage triage,
			ChatMessage assistantToolCallMessage,
			ToolCall toolCall,
			ComponentOwnerResult toolResult
	) {
		String resultJson = responseParser.serializeToolResult(toolResult);
		ChatMessage toolResultMessage = ChatMessage.toolResult(toolCall.id(), toolCall.function().name(), resultJson);

		ChatCompletionRequest request = requestFactory.createFinalResponseRequest(
				issue,
				triage,
				assistantToolCallMessage,
				toolResultMessage
		);
		ChatCompletionResponse response = llmClient.complete(request);
		String finalMessage = responseParser.extractFinalMessage(response);
		log.info("FINAL_RESPONSE message={}", finalMessage);
		return finalMessage;
	}
}
