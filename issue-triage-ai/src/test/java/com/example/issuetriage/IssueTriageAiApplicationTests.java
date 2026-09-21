package com.example.issuetriage;

import com.example.issuetriage.config.GroqProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "groq.api-key=test-only-groq-api-key")
class IssueTriageAiApplicationTests {

	@Autowired
	private GroqProperties groqProperties;

	@Test
	void contextLoadsWithTestOnlyGroqApiKey() {
		assertThat(groqProperties.apiKey()).isEqualTo("test-only-groq-api-key");
	}

}
