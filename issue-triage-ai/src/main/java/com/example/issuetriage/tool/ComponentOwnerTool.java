package com.example.issuetriage.tool;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ComponentOwnerTool {

	public static final String TOOL_NAME = "get_component_owner";

	private static final Map<String, String> OWNERS = Map.of(
			"login", "Identity Team",
			"authentication", "Identity Team",
			"payment", "Payments Team",
			"checkout", "Payments Team",
			"database", "Platform Team"
	);

	public ComponentOwnerResult execute(ComponentOwnerArguments arguments) {
		String component = arguments != null && arguments.component() != null
				? arguments.component().trim()
				: "";
		String owner = OWNERS.getOrDefault(component.toLowerCase(), "Unknown Team");
		return new ComponentOwnerResult(component, owner);
	}

	public String getComponentOwner(String component) {
		if (component == null || component.isBlank()) {
			return "Unknown Team";
		}
		return OWNERS.getOrDefault(component.trim().toLowerCase(), "Unknown Team");
	}
}
