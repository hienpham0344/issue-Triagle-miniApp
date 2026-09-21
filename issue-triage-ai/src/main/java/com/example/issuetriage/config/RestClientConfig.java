package com.example.issuetriage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GroqProperties.class)
public class RestClientConfig {

	@Bean
	RestClient groqRestClient(GroqProperties properties) {
		return RestClient.builder().baseUrl(properties.baseUrl()).build();
	}
}
