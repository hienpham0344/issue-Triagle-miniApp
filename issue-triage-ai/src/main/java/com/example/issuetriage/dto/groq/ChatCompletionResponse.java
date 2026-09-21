package com.example.issuetriage.dto.groq;

import java.util.List;

public record ChatCompletionResponse(List<Choice> choices) {

	public record Choice(ChatMessage message) {
	}
}
