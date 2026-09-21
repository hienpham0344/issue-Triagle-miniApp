package com.example.issuetriage.controller;

import com.example.issuetriage.dto.FinalTriageResponse;
import com.example.issuetriage.dto.TriageRequest;
import com.example.issuetriage.service.IssueTriageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues")
public class IssueTriageController {

	private final ObjectProvider<IssueTriageService> issueTriageService;

	public IssueTriageController(ObjectProvider<IssueTriageService> issueTriageService) {
		this.issueTriageService = issueTriageService;
	}

	@PostMapping("/triage")
	public FinalTriageResponse triage(@Valid @RequestBody TriageRequest request) {
		return issueTriageService.getObject().triage(request.issue());
	}
}
