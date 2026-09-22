package com.example.issuetriage.application;

import com.example.issuetriage.api.dto.FinalTriageResponse;

public interface IssueTriageService {

	FinalTriageResponse triage(String issue);
}
