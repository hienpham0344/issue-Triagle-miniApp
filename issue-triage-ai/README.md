# Issue Triage AI (`issue-triage-ai`)

A Spring Boot application that automates incident and defect triage using Google Gemini's OpenAI-compatible Chat Completions API. It combines **Structured Output** (via JSON Schema) and **Function Calling** into separate, deterministic phases.

---

## 1. Project Purpose

When production issues and software bugs are reported, triage teams need to rapidly determine:
1. Is this a valid software issue? (`status`)
2. How severe is it? (`severity`: P0, P1, P2, P3)
3. Which system component is affected? (`component`)
4. Is immediate escalation required? (`needs_urgent_response`)
5. Which team owns the affected component? (`componentOwner`)
6. What is the incident summary? (`finalMessage`)

`issue-triage-ai` orchestrates this end-to-end lifecycle safely and deterministically without agent loops, vector databases, or complex multi-agent frameworks.

---

## 2. High-Level Architecture

The project follows a clean layered architecture with strict separation of concerns:

```
src/main/java/com/example/issuetriage/
│
├── IssueTriageAiApplication.java       # Application entry point
│
├── api/                                # HTTP Transport Layer
│   ├── IssueTriageController.java      # REST endpoint (/api/issues/triage)
│   └── dto/
│       ├── TriageRequest.java          # Inbound request payload
│       └── FinalTriageResponse.java    # Final API response
│
├── application/                        # Orchestration Layer
│   ├── IssueTriageService.java         # Service interface
│   └── IssueTriageServiceImpl.java     # Orchestrates Call #1, validation, Call #2, tool execution, Call #3
│
├── domain/                             # Business Domain (Provider-agnostic)
│   ├── model/
│   │   ├── IssueTriage.java            # Domain representation of triage data
│   │   ├── Severity.java               # P0, P1, P2, P3
│   │   └── TriageStatus.java           # classified, insufficient_data, out_of_scope
│   └── validation/
│       └── IssueTriageValidator.java   # Semantic business rule validation
│
├── ai/                                 # AI Integration Layer
│   ├── LlmClient.java                  # Provider-agnostic LLM interface
│   ├── gemini/
│   │   ├── GeminiClient.java           # HTTP client for Gemini OpenAI-compatible API
│   │   ├── GeminiRequestFactory.java   # Builds Call #1, #2, and #3 request payloads
│   │   ├── GeminiResponseParser.java   # Parses typed triage, tool calls, and final messages
│   │   └── dto/                        # Transport DTOs
│   │       ├── ChatCompletionRequest.java
│   │       ├── ChatCompletionResponse.java
│   │       ├── ChatMessage.java
│   │       ├── ToolCall.java
│   │       └── ToolFunction.java
│   ├── prompt/
│   │   └── TriagePrompt.java           # Classifier instructions & severity rubric
│   └── schema/
│       └── IssueTriageSchema.java      # JSON Schema for Structured Output
│
├── tool/                               # Local Java Tools
│   ├── ComponentOwnerTool.java         # Local lookup for software component owners
│   ├── ComponentOwnerArguments.java    # Parsed tool arguments
│   └── ComponentOwnerResult.java       # Local tool execution result
│
├── config/                             # Spring Configuration
│   ├── GeminiProperties.java           # Configuration properties for Gemini
│   └── RestClientConfig.java           # Spring RestClient bean definition
│
└── exception/                          # Controlled Exceptions
    ├── LlmApiException.java
    ├── IssueTriageValidationException.java
    └── ToolExecutionException.java
```

---

## 3. Requirements

- **Java**: Java 21 or higher (OpenJDK / Oracle JDK)
- **Maven**: 3.9+ (or use the provided `mvnw` wrapper)
- **Google Gemini API Key**: from Google AI Studio

---

## 4. Gemini API Key Setup

Set the `GEMINI_API_KEY` environment variable in your terminal before launching the application.

### Windows PowerShell:
```powershell
$env:GEMINI_API_KEY="your_actual_gemini_api_key"
```

### Linux / macOS:
```bash
export GEMINI_API_KEY="your_actual_gemini_api_key"
```

> **Security Note**: Never commit your real API key to source control or store it in plain text files. A template is provided in `.env.example`.

---

## 5. Starting the Application

```powershell
mvn spring-boot:run
```

The application starts by default on port `8080`.

---

## 6. Endpoint

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/issues/triage`
- **Content-Type**: `application/json`

---

## 7. Request Example

```json
{
  "issue": "Production login API returns HTTP 500. Around 80% of users cannot sign in."
}
```

---

## 8. Structured Output (LLM Call #1)

The first Gemini call performs **issue classification only**.

```
User Issue Report
       │
       ▼
Gemini Chat Completions (OpenAI-compatible)
  - System Prompt (TriagePrompt)
  - User Prompt (issue text)
  - response_format (IssueTriageSchema JSON Schema)
  - NO tools / NO tool_choice
       │
       ▼
Typed IssueTriage Object
       │
       ▼
IssueTriageValidator (Semantic Business Rule Checks)
```

The JSON Schema enforces:
- `status`: enum (`classified`, `insufficient_data`, `out_of_scope`)
- `severity`: enum (`P0`, `P1`, `P2`, `P3`, or `null`)
- `component`: string or `null`
- `needs_urgent_response`: boolean
- `reason`: non-empty string

---

## 9. Function Calling Lifecycle (LLM Call #2 & Call #3)

Function Calling is triggered **only** when `status == CLASSIFIED` and `component` is non-blank.

```
IssueTriage (classified)
       │
       ▼
[Call #2] Gemini Tool Request
  - Tools: [ get_component_owner ]
  - tool_choice: required / forced get_component_owner
  - NO response_format
       │
       ▼
Gemini returns tool_call:
  {
    "id": "call_xyz",
    "type": "function",
    "function": {
      "name": "get_component_owner",
      "arguments": "{\"component\":\"login\"}"
    }
  }
       │
       ▼
[Local Java Execution]
  - Java reads tool_call and validates name
  - Java parses arguments using Jackson: ComponentOwnerArguments("login")
  - Java invokes ComponentOwnerTool.execute(...)
  - Result: ComponentOwnerResult("login", "Identity Team")
       │
       ▼
[Call #3] Gemini Tool Result Submission
  - Context messages: user issue context
  - Assistant message with tool_call
  - Tool message with matching tool_call_id, name, and serialized JSON result
       │
       ▼
Gemini returns final natural-language summary (finalMessage)
       │
       ▼
FinalTriageResponse returned to caller
```

---

## 10. Core Principle: LLM Selects, Java Executes

- **Gemini selects the function**: The model determines that `get_component_owner` should be invoked with the arguments `{"component": "login"}`.
- **Java executes the function**: The model **never** directly executes Java code or accesses internal services. The Java application parses the requested function name, validates it, runs `ComponentOwnerTool` locally, and feeds the result back to Gemini.

---

## 11. Example Responses

### Scenario 1: Classified Incident (P0)

**Request**:
```json
{
  "issue": "Production login API returns HTTP 500. Around 80% of users cannot sign in."
}
```

**Response**:
```json
{
  "triage": {
    "status": "classified",
    "severity": "P0",
    "component": "login",
    "needs_urgent_response": true,
    "reason": "Production login failure affecting 80% of users prevents authentication and represents a critical service outage."
  },
  "componentOwner": "Identity Team",
  "finalMessage": "A P0 incident has been identified in the login component, causing severe disruption for users. The Identity Team owns this component and should respond immediately."
}
```

### Scenario 2: Insufficient Data

**Request**:
```json
{
  "issue": "The app is broken."
}
```

**Response**:
```json
{
  "triage": {
    "status": "insufficient_data",
    "severity": null,
    "component": null,
    "needs_urgent_response": false,
    "reason": "The report lacks actionable diagnostic details, error messages, or information on which component is affected."
  },
  "componentOwner": null,
  "finalMessage": null
}
```

### Scenario 3: Out of Scope

**Request**:
```json
{
  "issue": "What should I eat for dinner?"
}
```

**Response**:
```json
{
  "triage": {
    "status": "out_of_scope",
    "severity": null,
    "component": null,
    "needs_urgent_response": false,
    "reason": "The input is a personal culinary question and not a technical defect or software issue."
  },
  "componentOwner": null,
  "finalMessage": null
}
```

---

## 12. Status and Severity Definitions

### Triage Status:
- `classified`: Clearly describes a specific defect/incident with sufficient context.
- `insufficient_data`: Report is vague or incomplete, making classification impossible.
- `out_of_scope`: Non-technical inquiries, greetings, or unrelated prompts.

### Severity Levels:
- **P0**: Critical production outage, widespread service loss, or critical data loss.
- **P1**: Major functionality severely degraded for many users without a workaround.
- **P2**: Moderate impact defect with available workarounds or limited blast radius.
- **P3**: Minor cosmetic issues, typos, or low-urgency non-blocking bugs.
