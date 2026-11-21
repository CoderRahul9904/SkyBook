# Phase 7 — AI Integration: AeroMate

You are acting as a Senior Java Spring Boot Developer and Spring AI Architect. Implement Phase 7 of the existing SkyBook project: **AeroMate — AI-powered Airline Assistant**.

Work directly in the existing project. Do not create a separate application or unnecessarily restructure existing code.

## 1. First inspect the existing project

Before implementation:

- Read `AGENTS.md`, the approved project proposal, SRS, architecture documentation, and existing implementation prompts.
- Inspect the existing `pom.xml`, application configuration, package structure, entities, repositories, services, controllers, security configuration, and database schema.
- Understand the existing flight search, flight details, booking, cancellation, and authentication implementations.
- Check the existing Spring Boot and Spring AI versions and use compatible dependencies and APIs.
- Inspect the configured Capgemini AI Gateway integration details, if present.
- Identify existing functionality that can be reused.

Do not assume classes, methods, endpoints, or dependencies exist. Do not duplicate existing business logic.

## 2. Objective

Implement AeroMate using these four mandatory Spring AI capabilities:

1. Retrieval-Augmented Generation (RAG)
2. Tool Calling
3. Persistent Chat Memory
4. Model Context Protocol (MCP)

Keep the existing modular monolith architecture.

Use the existing Gemini model through the Capgemini AI Gateway and its supported OpenAI-compatible integration. Do not introduce another LLM provider unless the existing gateway cannot support the required capability. If embeddings are unsupported by the gateway, identify a compatible embedding solution and document the decision.

## 3. RAG — Airline Knowledge Assistant

Implement a small airline-policy knowledge base containing Markdown documents covering:

- Baggage allowance
- Booking guidelines
- Cancellation policy
- Refund information
- Check-in guidelines
- Passenger requirements

Requirements:

- Store knowledge documents under an appropriate existing resources directory.
- Load documents using Spring AI document readers/loaders.
- Apply document transformation and text splitting.
- Generate embeddings and store them in PostgreSQL using pgvector if compatible with the existing environment.
- Configure the vector store and retrieval mechanism.
- Integrate retrieval with the existing Spring AI ChatClient.
- Ensure AeroMate uses retrieved information when answering airline-policy questions.
- If the answer is not available in the knowledge base, clearly communicate that instead of inventing airline policies.
- Avoid re-ingesting the same documents on every application startup unnecessarily.

Do not invent real airline policies. Clearly identify the policy documents as sample SkyBook policies.

## 4. Tool Calling — Reuse Existing Business Services

Implement read-only tools that allow AeroMate to retrieve live SkyBook information.

Implement these tools where supported by the existing application:

1. `searchFlights` — search available flights using origin, destination, travel date, passenger count, and cabin class.
2. `getFlightDetails` — retrieve flight schedule, fare, and cabin inventory details.
3. `getMyBookings` — retrieve bookings belonging to the authenticated customer.
4. `getBookingDetails` — retrieve details of a booking belonging to the authenticated customer using its PNR.

Requirements:

- Reuse existing FlightService and BookingService methods.
- Do not duplicate flight-search or booking business logic.
- Use appropriate input validation and clear tool descriptions.
- Never trust a user ID supplied by the LLM or chat request.
- Derive the authenticated customer from the application's trusted Spring Security context.
- Verify booking ownership before returning booking information.
- Do not expose passwords, tokens, or sensitive passenger information unnecessarily.
- Do not allow AI tools to create bookings, cancel bookings, change fares, update inventory, or perform admin operations.
- If the current security context cannot safely be accessed during tool execution, implement an explicit secure context propagation approach rather than weakening authorization.

## 5. Persistent Chat Memory

Implement conversation memory using Spring AI JDBC Chat Memory with PostgreSQL.

Requirements:

- Persist conversation messages in the database.
- Support multiple conversations for each authenticated customer.
- Generate or accept a conversation ID safely.
- Associate every conversation with its authenticated owner.
- Verify conversation ownership before retrieving or continuing it.
- Prevent one customer from accessing another customer's conversation.
- Preserve conversation context across requests and application restarts.
- Avoid implementing a second custom message-history system that duplicates Spring AI JDBC Chat Memory.
- Store only necessary conversation metadata separately if required.

A customer must be able to continue an existing conversation using its conversation ID after refreshing the application.

## 6. MCP — Model Context Protocol

Implement a minimal but functional MCP demonstration using Spring AI MCP support.

Requirements:

- Implement an MCP server exposing read-only flight search and flight details capabilities.
- Reuse existing application services wherever possible.
- Configure a compatible MCP transport supported by the current Spring AI version.
- Implement a small MCP client/demo component that connects to the server, discovers available tools, and invokes at least one tool.
- Demonstrate successful tool discovery and invocation through an automated test or runnable integration demonstration.
- Keep MCP separate from the normal REST API and normal LLM Tool Calling implementation.
- Do not create unnecessary microservices or a separate frontend for MCP.

Document how the MCP server and client can be executed and verified.

## 7. REST API Integration

Expose a clean REST endpoint for AeroMate using the existing `/api/v1` convention.

Implement the minimum required endpoints:

- `POST /api/v1/ai/chat` — send a message and receive an assistant response.
- `GET /api/v1/ai/conversations` — list the authenticated customer's conversations.
- `GET /api/v1/ai/conversations/{conversationId}` — retrieve an owned conversation.

Requirements:

- Use request and response DTOs.
- Validate incoming requests.
- Require authentication.
- Verify conversation ownership.
- Return consistent error responses using the existing exception-handling approach.
- Do not expose internal Spring AI objects directly through REST APIs.

## 8. Configuration and Security

- Keep API credentials outside source code.
- Use environment variables or the existing secure configuration mechanism.
- Do not commit real API keys, tokens, or passwords.
- Preserve existing JWT authentication and role-based authorization.
- Ensure AI endpoints require an authenticated user.
- Apply sensible request validation and error handling.
- Do not expose stack traces or internal gateway credentials in API responses.
- Do not weaken existing security rules to make AI integration work.

Update `application.properties` or the existing configuration files only where necessary. Provide an example environment-variable configuration without real secrets.

## 9. Testing — Mandatory Before Completion

Do not finish after generating code. Run the relevant tests and resolve implementation-related failures.

### A. Build verification

Run:

`.\mvnw.cmd clean test`

If necessary, also run:

`.\mvnw.cmd clean package`

Use the project's existing Maven wrapper and report the actual results.

### B. RAG verification

Verify:

- Knowledge documents load successfully.
- Embeddings are generated and stored.
- Relevant policy questions retrieve appropriate documents.
- Unsupported policy questions receive an honest fallback response.

### C. Tool Calling verification

Verify:

- Flight search tool returns actual database-backed results.
- Flight details tool returns valid flight information.
- Booking tools return only the authenticated customer's data.
- Unauthorized booking access is rejected.
- No AI tool can mutate bookings or inventory.

### D. Chat Memory verification

Verify:

- Messages persist across requests.
- Previous conversation context is available in subsequent messages.
- Different conversation IDs remain isolated.
- A customer cannot access another customer's conversation.
- Conversation history survives application restart, where the test environment supports it.

### E. MCP verification

Verify:

- MCP server starts successfully.
- Client discovers the registered tools.
- Client successfully invokes at least one flight-related tool.
- The response contains the expected application data.

### F. REST API verification

Verify:

- Authenticated chat request succeeds when the AI gateway is available.
- Conversation listing works.
- Conversation retrieval works.
- Invalid requests receive appropriate error responses.
- Unauthenticated requests are rejected.

Use unit tests and integration tests where practical. Mock external AI gateway calls in automated tests so that the test suite does not depend on API availability or consume unnecessary credits. Separately document any live gateway verification that requires credentials.

Do not claim a live AI integration test passed unless it was actually executed.

## 10. Documentation

Update or create the appropriate documentation under `docs/`:

- AI architecture and component responsibilities.
- RAG document ingestion and retrieval flow.
- Tool Calling design and security boundaries.
- JDBC Chat Memory persistence design.
- MCP server/client configuration and execution.
- Required environment variables.
- API endpoints and example requests.
- Commands to run and test the AI integration.
- Actual test results and known limitations.

Keep documentation consistent with the implemented code.

## 11. Strict implementation rules

- Preserve all previously completed phases.
- Do not modify existing flight search, booking, cancellation, or admin behavior unless a necessary compatibility fix is identified.
- Do not implement the React frontend in this phase.
- Do not implement payment integration.
- Do not add unrelated features.
- Do not introduce microservices.
- Do not duplicate existing business logic.
- Avoid unnecessary dependencies and abstractions.
- Do not silently change existing database entities or API contracts.
- Do not delete existing tests or weaken existing security checks.
- Follow the existing project's coding conventions.
- If a genuine blocker exists, explain it and implement the remaining independent functionality safely.

## 12. Execution strategy

Execute the complete phase in this order:

1. Inspect project and identify compatibility issues.
2. Confirm dependencies and configuration.
3. Implement RAG.
4. Implement secure read-only Tool Calling.
5. Implement persistent JDBC Chat Memory.
6. Implement MCP server and client demonstration.
7. Integrate the AeroMate REST APIs.
8. Add unit and integration tests.
9. Run Maven build and tests.
10. Fix implementation-related failures and rerun the relevant tests.
11. Update documentation.
12. Review changed files for unintended modifications.

Do not stop after creating a plan or asking for approval for every small implementation step. Make reasonable engineering decisions within the approved project scope and complete the phase.

If a decision would require changing the approved architecture, weakening security, introducing a new external service, or changing existing business behavior, stop and explain the issue rather than making that change silently.

## 13. Final report

When finished, provide:

1. Features implemented under each of the four Spring AI capabilities.
2. REST endpoints added.
3. Important files created or modified.
4. Dependencies added or changed.
5. Database/schema changes.
6. Maven test results: tests, failures, errors and build status.
7. RAG, Tool Calling, Memory and MCP verification results.
8. Any live integration tests not executed and why.
9. Remaining limitations or blockers.
10. Exact commands needed to run and verify AeroMate.

Only mark Phase 7 complete when the implementation and verification results support that conclusion.