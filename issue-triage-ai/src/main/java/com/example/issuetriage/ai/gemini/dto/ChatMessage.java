package com.example.issuetriage.ai.gemini.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

	@JsonProperty("role")
	private String role;

	@JsonProperty("content")
	private String content;

	@JsonProperty("tool_calls")
	private List<ToolCall> toolCalls;

	@JsonProperty("tool_call_id")
	private String toolCallId;

	@JsonProperty("name")
	private String name;

	@JsonProperty("thought_signature")
	private String thoughtSignature;

	private final Map<String, Object> extraProperties = new HashMap<>();

	public ChatMessage() {
	}

	public ChatMessage(String role, String content, List<ToolCall> toolCalls, String toolCallId, String name) {
		this.role = role;
		this.content = content;
		this.toolCalls = toolCalls;
		this.toolCallId = toolCallId;
		this.name = name;
	}

	public String role() {
		return role;
	}

	public String content() {
		return content;
	}

	public List<ToolCall> toolCalls() {
		return toolCalls;
	}

	public String toolCallId() {
		return toolCallId;
	}

	public String name() {
		return name;
	}

	public String thoughtSignature() {
		return thoughtSignature;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setToolCalls(List<ToolCall> toolCalls) {
		this.toolCalls = toolCalls;
	}

	public void setToolCallId(String toolCallId) {
		this.toolCallId = toolCallId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setThoughtSignature(String thoughtSignature) {
		this.thoughtSignature = thoughtSignature;
	}

	@JsonAnyGetter
	public Map<String, Object> getExtraProperties() {
		return extraProperties;
	}

	@JsonAnySetter
	public void setExtraProperty(String key, Object value) {
		extraProperties.put(key, value);
	}

	public static ChatMessage system(String content) {
		return new ChatMessage("system", content, null, null, null);
	}

	public static ChatMessage user(String content) {
		return new ChatMessage("user", content, null, null, null);
	}

	public static ChatMessage assistant(String content) {
		return new ChatMessage("assistant", content, null, null, null);
	}

	public static ChatMessage assistantToolCall(List<ToolCall> toolCalls) {
		return new ChatMessage("assistant", null, toolCalls, null, null);
	}

	public static ChatMessage toolResult(String toolCallId, String toolName, String resultJson) {
		return new ChatMessage("tool", resultJson, null, toolCallId, toolName);
	}
}
