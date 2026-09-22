package com.example.issuetriage.ai.gemini.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolCall {

	@JsonProperty("id")
	private String id;

	@JsonProperty("type")
	private String type;

	@JsonProperty("function")
	private ToolFunction function;

	@JsonProperty("thought_signature")
	private String thoughtSignature;

	private final Map<String, Object> extraProperties = new HashMap<>();

	public ToolCall() {
	}

	public ToolCall(String id, String type, ToolFunction function) {
		this.id = id;
		this.type = type;
		this.function = function;
	}

	public String id() {
		return id;
	}

	public String type() {
		return type;
	}

	public ToolFunction function() {
		return function;
	}

	public String thoughtSignature() {
		return thoughtSignature;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setFunction(ToolFunction function) {
		this.function = function;
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
}
