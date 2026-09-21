package com.example.issuetriage.client;

import com.example.issuetriage.config.GroqProperties;
import com.example.issuetriage.exception.GroqApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GroqClientTests {

	private static final String API_KEY = "test-groq-api-key";
	private MockRestServiceServer server;
	private GroqClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://groq.test/openai/v1");
		server = MockRestServiceServer.bindTo(builder).build();
		client = new GroqClient(builder.build(), new GroqProperties("https://groq.test/openai/v1", API_KEY, "llama-3.3-70b-versatile"));
	}

	@Test
	void completePostsJsonBodyWithBearerAuthenticationAndParsesResponse() {
		server.expect(requestTo("https://groq.test/openai/v1/chat/completions"))
			.andExpect(method(org.springframework.http.HttpMethod.POST))
			.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY))
			.andExpect(content().json("""
				{"model":"llama-3.3-70b-versatile","messages":[{"role":"user","content":"triage this"}]}
				"""))
			.andRespond(withSuccess("""
				{"id":"chatcmpl-1","choices":[{"message":{"content":"classified"}}]}
				""", MediaType.APPLICATION_JSON));

		JsonNode response = client.complete(new RequestBody("llama-3.3-70b-versatile", new Message("user", "triage this")));

		assertThat(response.get("id").asString()).isEqualTo("chatcmpl-1");
		assertThat(response.get("choices").get(0).get("message").get("content").asString()).isEqualTo("classified");
		server.verify();
	}

	@Test
	void completeMapsNonSuccessResponseWithoutDisclosingSecrets() {
		server.expect(requestTo("https://groq.test/openai/v1/chat/completions"))
			.andExpect(method(org.springframework.http.HttpMethod.POST))
			.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY))
			.andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
				.contentType(MediaType.APPLICATION_JSON)
				.body("{\"error\":{\"message\":\"upstream failure\"}}"));

		Throwable thrown = catchThrowable(() -> client.complete(new RequestBody("test", new Message("user", "triage this"))));

		assertThat(thrown).isInstanceOf(GroqApiException.class);
		assertThat(thrown.getMessage())
			.contains("429")
			.contains("error details omitted")
			.doesNotContain(API_KEY)
			.doesNotContain("upstream failure");
		server.verify();
	}

	private record RequestBody(String model, Message[] messages) {
		private RequestBody(String model, Message message) {
			this(model, new Message[] { message });
		}
	}

	private record Message(String role, String content) {
	}
}
