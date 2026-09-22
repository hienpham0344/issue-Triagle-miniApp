package com.example.issuetriage.ai.schema;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class IssueTriageSchema {

	private IssueTriageSchema() {
	}

	public static Map<String, Object> getSchema() {
		return Map.of(
				"type", "object",
				"additionalProperties", false,
				"required", List.of("status", "severity", "component", "needs_urgent_response", "reason"),
				"properties", Map.of(
						"status", Map.of(
								"type", "string",
								"enum", List.of("classified", "insufficient_data", "out_of_scope")
						),
						"severity", Map.of(
								"type", List.of("string", "null"),
								"enum", Arrays.asList("P0", "P1", "P2", "P3", null)
						),
						"component", Map.of(
								"type", List.of("string", "null")
						),
						"needs_urgent_response", Map.of(
								"type", "boolean"
						),
						"reason", Map.of(
								"type", "string"
						)
				)
		);
	}
}
