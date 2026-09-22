package com.example.issuetriage.ai;

import com.example.issuetriage.ai.gemini.dto.ChatCompletionRequest;
import com.example.issuetriage.ai.gemini.dto.ChatCompletionResponse;

public interface LlmClient {

	ChatCompletionResponse complete(ChatCompletionRequest request);
}
