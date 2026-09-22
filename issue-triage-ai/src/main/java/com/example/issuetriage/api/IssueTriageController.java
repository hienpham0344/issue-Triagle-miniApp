package com.example.issuetriage.api;

import com.example.issuetriage.api.dto.FinalTriageResponse;
import com.example.issuetriage.api.dto.TriageRequest;
import com.example.issuetriage.application.IssueTriageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues")
public class IssueTriageController {

	private final IssueTriageService issueTriageService;

	public IssueTriageController(IssueTriageService issueTriageService) {
		this.issueTriageService = issueTriageService;
	}

	@PostMapping("/triage")
	public FinalTriageResponse triage(@Valid @RequestBody TriageRequest request) {
		return issueTriageService.triage(request.issue());
	}
}
