package com.example.issuetriage.ai.gemini;

import com.example.issuetriage.ai.LlmClient;
import com.example.issuetriage.ai.gemini.dto.ChatCompletionRequest;
import com.example.issuetriage.ai.gemini.dto.ChatCompletionResponse;
import com.example.issuetriage.config.GeminiProperties;
import com.example.issuetriage.exception.LlmApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@Component
public class GeminiClient implements LlmClient {

	private final RestClient restClient;
	private final GeminiProperties properties;

	public GeminiClient(RestClient geminiRestClient, GeminiProperties properties) {
		this.restClient = geminiRestClient;
		this.properties = properties;
	}

	@Override
	public ChatCompletionResponse complete(ChatCompletionRequest request) {
		return restClient.post()
				.uri("/chat/completions")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
				.body(request)
				.retrieve()
				.onStatus(status -> status.isError(), (req, resp) -> {
					String errorDetails;
					try {
						byte[] bytes = resp.getBody().readAllBytes();
						errorDetails = bytes.length == 0 ? "Empty response body" : new String(bytes, StandardCharsets.UTF_8);
					} catch (Exception ex) {
						errorDetails = "Unable to read error body";
					}
					throw new LlmApiException(resp.getStatusCode().value(), errorDetails);
				})
				.body(ChatCompletionResponse.class);
	}
}
