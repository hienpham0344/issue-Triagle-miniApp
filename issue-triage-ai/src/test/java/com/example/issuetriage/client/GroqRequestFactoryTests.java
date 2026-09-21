package com.example.issuetriage.client;

import com.example.issuetriage.config.GroqProperties;
import com.example.issuetriage.dto.groq.ChatCompletionRequest;
import com.example.issuetriage.model.IssueTriage;
import com.example.issuetriage.model.Severity;
import com.example.issuetriage.model.TriageStatus;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class GroqRequestFactoryTests {

	private final JsonMapper mapper = JsonMapper.builder().build();
	private final GroqRequestFactory factory = new GroqRequestFactory(
		new GroqProperties("https://groq.test/openai/v1", "test-key", "openai/gpt-oss-20b"), mapper);

	@Test
	void structuredTriageCreatesStrictIssueTriageSchemaWithoutTools() {
		ChatCompletionRequest request = factory.structuredTriage("The API returns 500 after sign-in.");

		JsonNode root = mapper.valueToTree(request);
		JsonNode schema = root.path("response_format").path("json_schema").path("schema");
		assertThat(root.path("model").asString()).isEqualTo("openai/gpt-oss-20b");
		assertThat(root.path("messages").size()).isEqualTo(2);
		assertThat(root.path("messages").get(0).path("role").asString()).isEqualTo("system");
		assertThat(root.path("messages").get(1).path("role").asString()).isEqualTo("user");
		assertThat(root.path("messages").get(1).path("content").asString())
			.isEqualTo("The API returns 500 after sign-in.");
		assertThat(root.has("tools")).isFalse();
		assertThat(root.path("response_format").path("type").asString()).isEqualTo("json_schema");
		assertThat(root.path("response_format").path("json_schema").path("name").asString()).isEqualTo("issue_triage");
		assertThat(root.path("response_format").path("json_schema").path("strict").asBoolean()).isTrue();
		assertThat(schema.path("type").asString()).isEqualTo("object");
		assertThat(schema.path("additionalProperties").asBoolean()).isFalse();
		assertThat(schema.path("required")).extracting(JsonNode::asString)
			.containsExactlyInAnyOrder("status", "severity", "component", "needs_urgent_response", "reason");
		assertThat(schema.path("properties").path("status").path("enum")).extracting(JsonNode::asString)
			.containsExactly("classified", "insufficient_data", "out_of_scope");
		assertThat(schema.path("properties").path("severity").path("type")).extracting(JsonNode::asString)
			.containsExactly("string", "null");
		JsonNode severityEnum = schema.path("properties").path("severity").path("enum");
		assertThat(severityEnum).extracting(JsonNode::asString).containsExactly("P0", "P1", "P2", "P3", "");
		assertThat(severityEnum.get(4).isNull()).isTrue();
		assertThat(schema.path("properties").path("component").path("type")).extracting(JsonNode::asString)
			.containsExactly("string", "null");
		assertThat(schema.path("properties").path("needs_urgent_response").path("type").asString()).isEqualTo("boolean");
		assertThat(schema.path("properties").path("reason").path("type").asString()).isEqualTo("string");
	}

	@Test
	void readIssueTriageDeserializesGroqContentIntoTypedModel() throws Exception {
		JsonNode groqResponse = mapper.readTree("""
			{"choices":[{"message":{"content":"{\\"status\\":\\"classified\\",\\"severity\\":\\"P1\\",\\"component\\":\\"authentication\\",\\"needs_urgent_response\\":true,\\"reason\\":\\"Sign-in is unavailable.\\"}"}}]}
			""");

		IssueTriage triage = factory.readIssueTriage(groqResponse);

		assertThat(triage).isEqualTo(new IssueTriage(
			TriageStatus.CLASSIFIED, Severity.P1, "authentication", true, "Sign-in is unavailable."));
	}
}
