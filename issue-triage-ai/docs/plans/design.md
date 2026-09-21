# Issue Triage AI — Design

## Mục tiêu

Xây dựng API `POST /api/issues/triage` minh họa tách biệt Structured Output,
business validation và local function calling của Groq. Ứng dụng chủ động điều
phối một flow cố định; model chỉ đề nghị tool, không thể gọi Java trực tiếp.

## Quyết định kiến trúc

Code dùng package-by-layer. Controller chỉ validate HTTP request và gọi
`IssueTriageService`; service điều phối ba lần gọi model; `client` chỉ chịu
trách nhiệm wire format/API Groq; `ToolDispatcher` whitelist một tool duy nhất
và gọi `ComponentOwnerService` cục bộ. Domain model không phụ thuộc Spring hay
Groq DTO.

`GroqRequestFactory` tạo ba request độc lập:

1. Call #1 có system prompt riêng, user issue, và `response_format.json_schema`
   strict cho `IssueTriage`; không gửi tools.
2. Call #2 nhận triage đã qua `IssueTriageValidator`, gửi đúng một function tool
   `get_component_owner` cùng `tool_choice: "required"`; không gửi
   `response_format`.
3. Call #3 tái tạo conversation gồm system/user/context, assistant message có
   chính tool call của Call #2, rồi một `role: tool` chứa JSON result với cùng
   `tool_call_id`; không gửi tool nữa.

Với status khác `CLASSIFIED`, orchestration dừng sau Call #1 + validation và tạo
response giải thích không thể route; không có Call #2/Call #3. Đây là ngoại lệ
có chủ đích để tránh ép tool call khi không có component hợp lệ.

## Dữ liệu và validation

`IssueTriage` là record có enum status/severity. Jackson 3 (`tools.jackson.databind`)
deserialize `choices[0].message.content`; annotations `com.fasterxml.jackson.annotation`
map snake_case wire values. JSON Schema bảo đảm hình dạng, còn
`IssueTriageValidator` bảo đảm quy tắc nghiệp vụ: `CLASSIFIED` cần severity,
component không blank, reason không blank; hai status còn lại phải severity null
và không urgent.

`ToolDispatcher` deserialize `function.arguments` vào
`GetComponentOwnerArguments`, kiểm tra tool name chính xác và component không
blank, rồi chuyển đến `ComponentOwnerService`. Unknown tool hoặc malformed
arguments là lỗi 4xx rõ ràng và không bao giờ chạy service.

## Tích hợp và lỗi

`GroqClient` dùng Spring `RestClient`, base URL/property/environment là
`groq.base-url`, `groq.api-key`, `groq.model`. API key chỉ đi trong header
Authorization, không có log. HTTP/API failure được chuyển thành
`GroqApiException`; domain/input lỗi thành `IssueTriageValidationException` /
`UnsupportedToolException`; `GlobalExceptionHandler` trả ProblemDetail phù hợp.
Log INFO/DEBUG mang marker cho ba LLM calls và tool lifecycle, không log secrets.

## Kiểm thử và vận hành

Tất cả unit/controller/client tests mock Groq hoặc HTTP server và không cần key
hay network. Service test dùng `InOrder` để chứng minh Call #1 → validation →
Call #2 → local tool → Call #3, và chứng minh tool result không phải REST
response. Build ở môi trường này dùng Maven cài sẵn cùng JDK 24 để compile
`--release 21`, vì wrapper PowerShell hiện lỗi trước khi Maven chạy; CI/local
có JDK 21 vẫn dùng `mvnw.cmd test` bình thường.

## Ràng buộc

Không Spring AI, LangChain4j, WebFlux, database, RAG, memory, agent loop, retry
tự chủ, multi-agent, frontend hoặc secret trong repository. Groq official docs
(kiểm tra 2026-09-21) xác nhận `openai/gpt-oss-20b` hỗ trợ strict JSON Schema và
tool use; strict schema yêu cầu required fields và `additionalProperties: false`.
