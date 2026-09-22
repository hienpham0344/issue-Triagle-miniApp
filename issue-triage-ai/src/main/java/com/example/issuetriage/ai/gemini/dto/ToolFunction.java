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
public class ToolFunction {

	@JsonProperty("name")
	private String name;

	@JsonProperty("arguments")
	private String arguments;

	@JsonProperty("thought_signature")
	private String thoughtSignature;

	private final Map<String, Object> extraProperties = new HashMap<>();

	public ToolFunction() {
	}

	public ToolFunction(String name, String arguments) {
		this.name = name;
		this.arguments = arguments;
	}

	public String name() {
		return name;
	}

	public String arguments() {
		return arguments;
	}

	public String thoughtSignature() {
		return thoughtSignature;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
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
