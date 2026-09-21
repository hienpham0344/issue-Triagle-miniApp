package com.example.issuetriage.exception;

public class GroqApiException extends RuntimeException {

	public GroqApiException(int statusCode, String detail) {
		super("Groq API request failed with HTTP status " + statusCode + ": " + detail);
	}
}
