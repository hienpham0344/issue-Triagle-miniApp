package com.example.issuetriage.client;

import com.example.issuetriage.config.GroqProperties;
import com.example.issuetriage.exception.GroqApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Component
public class GroqClient {

	private final RestClient restClient;
	private final GroqProperties properties;

	public GroqClient(RestClient groqRestClient, GroqProperties properties) {
		this.restClient = groqRestClient;
		this.properties = properties;
	}

	public JsonNode complete(Object requestBody) {
		return restClient.post()
			.uri("/chat/completions")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
			.body(requestBody)
			.retrieve()
			.onStatus(status -> status.isError(), (request, response) -> {
				String detail = response.getBody().readAllBytes().length == 0 ? "no error details" : "error details omitted";
				throw new GroqApiException(response.getStatusCode().value(), detail);
			})
			.body(JsonNode.class);
	}
}
