package com.example.issuetriage.service;

import com.example.issuetriage.dto.FinalTriageResponse;

/**
 * Orchestration seam implemented in Task 8.
 */
public interface IssueTriageService {

	FinalTriageResponse triage(String issue);
}
