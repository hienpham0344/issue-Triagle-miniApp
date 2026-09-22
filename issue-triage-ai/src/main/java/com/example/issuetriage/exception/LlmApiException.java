package com.example.issuetriage.exception;

public class LlmApiException extends RuntimeException {

	private final int statusCode;

	public LlmApiException(int statusCode, String detail) {
		super("LLM API request failed with HTTP status " + statusCode + ": " + detail);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
