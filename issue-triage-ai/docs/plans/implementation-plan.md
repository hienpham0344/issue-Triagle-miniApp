# Issue Triage AI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans` task-by-task. Steps use checkbox syntax.

**Goal:** Implement a Spring MVC API that triages an issue through three explicit Groq calls and a locally dispatched ownership tool.

**Architecture:** Controller delegates to one orchestration service. The service uses a Jackson-3 Groq client/request factory, validates typed triage data, dispatches exactly one local tool, and returns its tool output to Groq for the final response. DTOs isolate the OpenAI-compatible wire format from domain models.

**Tech Stack:** Java 21 bytecode, Spring Boot 4.1.1 MVC, Spring RestClient, Jakarta Validation, Jackson 3 (`tools.jackson.databind`), JUnit 5, Mockito, MockMvc, MockRestServiceServer.

**Spec:** `docs/plans/design.md`

## Global Constraints

- Use package-by-layer below `com.example.issuetriage`; never feature packages.
- Use plain Java + Spring Web MVC; do not add Spring AI, LangChain4j, WebFlux, database, RAG, memory, agent loops, or frontend.
- API key comes only from `GROQ_API_KEY`; do not log keys or Authorization headers.
- Call #1 uses strict JSON Schema and no tools; Call #2 uses one function tool and no `response_format`; Call #3 sends the matching tool result back and no tools.
- Every logic class follows RED → GREEN → REFACTOR; every green checkpoint is a conventional commit.
- Run local Maven commands with JDK 24 in this host to compile release 21 because its wrapper is incompatible with the host PowerShell; the project remains Java 21.

## Review Focus

- Blank/whitespace `issue` must return HTTP 400 before orchestration; test in Task 1.
- A non-classified triage must not reach Call #2 or Call #3; test in Task 8.
- A tool call with unknown name must never reach `ComponentOwnerService`; test in Task 5.
- Tool arguments with blank component must produce a client error rather than an owner lookup; test in Task 5.
- Groq non-2xx responses must not leak secret headers/body; test in Task 2.

---

### Task 1: Web boundary (CP1)

**Files:** create `dto/TriageRequest.java`, `dto/FinalTriageResponse.java`, `controller/IssueTriageController.java`, controller test; modify application wiring if needed.

**Interfaces:** Produces `POST /api/issues/triage`, accepting `TriageRequest(@NotBlank String issue)` and delegating `IssueTriageService.triage(String)`.

- [ ] Write MockMvc test for blank/whitespace issue expecting 400 and no service call.
- [ ] Run that test; expected RED because endpoint is absent.
- [ ] Add minimal DTO/controller with `@Valid`; make controller delegate only to service.
- [ ] Re-run controller test; expected GREEN, then refactor names/imports only.
- [ ] Run Maven test suite; expected 0 failures; commit `feat: add issue triage web boundary`.

### Task 2: Groq configuration and HTTP client (CP2)

**Files:** create `config/GroqProperties.java`, `config/RestClientConfig.java`, `client/GroqClient.java`, `exception/GroqApiException.java`, client test; modify `application.yaml`.

**Interfaces:** Produces `GroqClient.complete(ChatCompletionRequest)` and binds `groq.base-url`, `groq.api-key`, `groq.model`.

- [ ] Write MockRestServiceServer tests asserting POST `/chat/completions`, JSON body forwarding, bearer header, and non-2xx mapping without secret disclosure.
- [ ] Run client test; expected RED because client/config do not exist.
- [ ] Implement minimal `RestClient` client and properties; read error body only for sanitized exception detail.
- [ ] Re-run client test then entire suite; expected GREEN; commit `feat: add Groq RestClient integration`.

### Task 3: Structured triage wire model and request factory (CP3)

**Files:** create `model/{IssueTriage,TriageStatus,Severity}.java`, `dto/groq/{ChatCompletionRequest,ChatCompletionResponse,ChatMessage}.java`, `client/GroqRequestFactory.java`, client/model tests.

**Interfaces:** Produces `structuredTriage(String)` and `readIssueTriage(ChatCompletionResponse)`; `IssueTriage` maps snake_case JSON.

- [ ] Write tests that assert the Call #1 request has strict `issue_triage` schema, all five required fields, nullable severity/component, `additionalProperties:false`, and no tools; add sample JSON deserialization test.
- [ ] Run scoped tests; expected RED.
- [ ] Implement records/enums and Jackson-3 factory/deserialization without prose parsing.
- [ ] Re-run scoped and full suites; expected GREEN; commit `feat: add structured issue triage request`.

### Task 4: Business validation (CP4)

**Files:** create `validation/IssueTriageValidator.java`, `exception/IssueTriageValidationException.java`, validator test.

**Interfaces:** Produces `void validate(IssueTriage triage)`.

- [ ] First write the six tests: classified-valid passes; classified-null severity fails; classified-blank component fails; insufficient-null severity passes; insufficient-P1 fails; out-of-scope urgent true fails.
- [ ] Run validator tests; expected RED.
- [ ] Implement only the specified status rules and clear exception messages.
- [ ] Re-run validator and full tests; expected GREEN; commit `feat: validate issue triage business rules`.

### Task 5: Tool schema, DTO and dispatcher (CP5)

**Files:** create `dto/groq/{ToolCall,GetComponentOwnerArguments}.java`, `service/ToolDispatcher.java`, `exception/UnsupportedToolException.java`, service/client tests; modify `GroqRequestFactory`.

**Interfaces:** Produces `functionCall(String, IssueTriage)`, `finalizeWithTool(...)`, and `ToolDispatcher.dispatch(String,String)` returning `ComponentOwnerResult`.

- [ ] Write tests that Call #2 exposes exactly `get_component_owner`, uses no response_format, and dispatcher accepts that name but rejects unknown/blank arguments.
- [ ] Run tests; expected RED.
- [ ] Implement closed function parameters schema and whitelist switch; parse arguments via Jackson 3.
- [ ] Re-run tests/full suite; expected GREEN; commit `feat: add local tool dispatch contract`.

### Task 6: Component ownership implementation (CP6)

**Files:** create `model/ComponentOwnerResult.java`, `service/ComponentOwnerService.java`, service test.

**Interfaces:** Produces `ComponentOwnerResult getComponentOwner(String component)`.

- [ ] Write unit tests for payment → Payments Team, authentication → Identity Team, and unknown → Platform Triage Team.
- [ ] Run tests; expected RED.
- [ ] Implement normalized in-memory map with checkout/search mappings and default.
- [ ] Re-run tests/full suite; expected GREEN; commit `feat: add component owner lookup`.

### Task 7: Tool-result conversation (CP7)

**Files:** modify `GroqRequestFactory`, `ChatMessage`, wire DTO tests.

**Interfaces:** `finalizeWithTool` must include assistant `tool_calls` from Call #2 then a tool message with same `tool_call_id` and JSON owner content.

- [ ] Write test inspecting Call #3 messages for assistant tool_calls, role `tool`, matching ID, and serialized result; assert no tools/response_format.
- [ ] Run test; expected RED.
- [ ] Implement minimal conversation construction.
- [ ] Re-run tests/full suite; expected GREEN; commit `feat: return tool result to Groq`.

### Task 8: Orchestration and final response (CP8)

**Files:** create `service/IssueTriageService.java`, service test; modify DTO/factory only if tests reveal missing fields.

**Interfaces:** Produces `FinalTriageResponse triage(String issue)`.

- [ ] Write mock `GroqClient` tests with `InOrder`: Call #1, validation, Call #2, local dispatcher, Call #3; assert response uses Call #3 content and non-classified skips Call #2/#3.
- [ ] Run tests; expected RED.
- [ ] Implement fixed orchestration and trace logs; never expose a tool result directly to controller.
- [ ] Re-run tests/full suite; expected GREEN; commit `feat: orchestrate three-call issue triage`.

### Task 9: Error translation and trace logging (CP9)

**Files:** create `exception/GlobalExceptionHandler.java`, controller test; modify service/client logging.

**Interfaces:** Maps validation/unsupported-tool to 400 and Groq failures to 502 as ProblemDetail.

- [ ] Write tests for domain errors → 400 and `GroqApiException` → 502; capture no secret output.
- [ ] Run tests; expected RED.
- [ ] Implement centralized advice and markers `[TRIAGE]`, `[LLM CALL #n]`, `[TOOL CALL]`, `[TOOL RESULT]`, `[FINAL RESPONSE]` with safe fields only.
- [ ] Re-run tests/full suite; expected GREEN; commit `feat: add triage error handling and tracing`.

### Task 10: Learning docs and environment hygiene (CP10)

**Files:** create `.env.example`, `README.md`, `docs/uoc_tinh_chi_phi.html`; modify `.gitignore`.

**Interfaces:** Documents start/run/curl flow, fixed workflow versus autonomous agent, three calls, cost formula, current official-price reference/date, and exclusions.

- [ ] Write doc-content checks for `.env` ignored and no real API key; expected RED if hygiene is incomplete.
- [ ] Add concise documentation and cost calculator assumptions; use Groq official model page pricing captured 2026-09-21, marking it configurable and free-tier development separately.
- [ ] Run full Maven suite and a secret scan; expected GREEN/no secrets; commit `docs: add triage learning and cost guides`.

## Self-review

Coverage maps CP1–CP10 one-to-one. Every Definition-of-Done behavior has an owning task: three calls (3,5,7,8), business validation (4), local tool (5,6), errors/logging (2,9), and operational artifacts (10). The plan contains no unresolved placeholders or ambiguous implementation handoffs. It keeps the model integration isolated and does not introduce prohibited systems.
