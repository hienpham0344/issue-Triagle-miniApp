package com.example.issuetriage;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.issuetriage.dto.FinalTriageResponse;
import com.example.issuetriage.service.IssueTriageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@AutoConfigureMockMvc
class IssueTriageEndpointContractTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private IssueTriageService issueTriageService;

	@Test
	void blankIssueReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/issues/triage")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"issue\":\"   \"}"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(issueTriageService);
	}

	@Test
	void validIssueDelegatesExactTextAndSerializesServiceResponse() throws Exception {
		String issue = "Payment fails after checkout";
		given(issueTriageService.triage(issue))
				.willReturn(new FinalTriageResponse("Route this to Payments Team."));

		mockMvc.perform(post("/api/issues/triage")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"issue\":\"Payment fails after checkout\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.response").value("Route this to Payments Team."));

		verify(issueTriageService).triage(issue);
	}
}
