# SkyBook Phase 7 — AeroMate Handoff

## Purpose

This document transfers the current Phase 7 implementation context to another coding model. Phase 7 has not been implemented yet. The next model must continue implementation directly in this workspace without restarting the project or rewriting Phases 1–6.

## Workspace

Repository root:

`c:\Users\rsantosh\OneDrive - Capgemini\Documents\GitHub\SkyBook`

Backend root:

`c:\Users\rsantosh\OneDrive - Capgemini\Documents\GitHub\SkyBook\backend`

## Authoritative Instructions

The primary specification is:

- `.github/prompts/07-aeromate-ai-integration-prompt.md`

Also read:

- `AGENTS.md`
- `docs/01-project-proposal/PROJECT_PROPOSAL.md`
- `docs/02-srs/SRS.md`
- `docs/03-uml-diagrams/class/SKYBOOK_CLASS_DIAGRAM.md`
- `docs/04-er-diagram/SKYBOOK_ER_DIAGRAM.md`
- `docs/05-database-documentation/DATABASE_DESIGN.md`
- Existing previous-phase prompt files under `.github/prompts/`

## Completed Previous Phases

Phases 1–6 are present in the existing backend.

Implemented modules:

- Authentication and JWT
- Flight search and details
- Booking lifecycle
- Booking history and PNR lookup
- Booking cancellation
- Admin airport management
- Admin flight management
- Admin fare and inventory management
- Admin booking review and cancellation
- PostgreSQL/Hibernate integration
- Swagger/OpenAPI

No AI, frontend, payment, MCP, or Spring AI implementation is currently present. The `com.skybook.ai` package contains only `package-info.java`.

## Current Runtime

- Java: 21 configured in Maven; runtime observed as Java 25.0.2
- Spring Boot: 4.1.2
- Maven: wrapper 3.10.1
- PostgreSQL: localhost:5432/skybook
- PostgreSQL driver: present
- Hibernate: `ddl-auto=update`
- Spring AI: initially absent, now partially dependency-added

## Spring AI Version

Spring AI `2.0.0` is being used.

Confirmed local cached/installed modules include:

- `spring-ai-openai`
- `spring-ai-starter-model-openai`
- `spring-ai-starter-model-chat-memory-repository-jdbc`
- `spring-ai-starter-vector-store-pgvector`
- `spring-ai-pgvector-store`
- `spring-ai-markdown-document-reader`
- `spring-ai-rag`
- `spring-ai-vector-store`
- `spring-ai-model`
- `spring-ai-commons`
- `spring-ai-client-chat`

MCP dependencies for 2.0.0 were not confirmed locally or resolved successfully. MCP starter coordinates from online documentation correspond to newer 2.1.0-RC2 artifacts and may require Spring Boot 4.2 compatibility.

## POM State

The POM has an inserted Spring AI BOM and properties:

- `spring-ai.version = 2.0.0`
- `spring-ai-bom` dependency management
- Spring AI OpenAI starter
- Spring AI JDBC chat-memory starter
- Spring AI PGVector starter
- Markdown document reader
- Spring AI RAG module

Verify the POM before editing because another model or formatter may have changed it.

## Gateway Configuration

`application.properties` currently contains local Capgemini/OpenAI-compatible configuration properties:

- `spring.ai.openai.base-url=https://openai.generative.engine.capgemini.com/v1`
- `spring.ai.openai.chat.options.model=<configured model>`
- `spring.ai.openai.api-key=<secret is present locally>`

Do not print or repeat the actual API key.

The key is currently stored directly in the local, git-ignored `application.properties`. This is a security weakness relative to project rules. The implementation should preserve local usability while transitioning to environment-variable overrides and an example properties file.

Before live gateway verification:

- Confirm chat model accepts OpenAI-compatible requests.
- Confirm whether embedding models are exposed.
- Do not assume the configured chat model also supports embeddings.
- If embeddings are unsupported, implement a documented fallback such as a compatible external embedding provider or Spring AI `SimpleVectorStore` for tests. Pgvector remains the approved PostgreSQL target where compatible.

## Existing REST APIs

Authentication:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

Flight:

- `GET /api/v1/flights/search`
- `GET /api/v1/flights/{flightId}`

Bookings:

- `POST /api/v1/bookings`
- `GET /api/v1/bookings/my`
- `GET /api/v1/bookings/{pnr}`
- `PATCH /api/v1/bookings/{pnr}/cancel`

Admin:

- `POST /api/v1/admin/airports`
- `GET /api/v1/admin/airports`
- `GET /api/v1/admin/airports/{id}`
- `PUT /api/v1/admin/airports/{id}`
- `POST /api/v1/admin/flights`
- `GET /api/v1/admin/flights`
- `GET /api/v1/admin/flights/{id}`
- `PUT /api/v1/admin/flights/{id}`
- `PUT /api/v1/admin/flights/{flightId}/fares/{cabinClass}`
- `PUT /api/v1/admin/flights/{flightId}/inventory/{cabinClass}`
- `GET /api/v1/admin/flights/{flightId}/cabins`
- `GET /api/v1/admin/bookings`
- `GET /api/v1/admin/bookings/{pnr}`
- `PATCH /api/v1/admin/bookings/{pnr}/cancel`

No operational DELETE endpoints for airports or flights.

## Existing Services to Reuse

Authentication:

- `AuthService`
- `JwtService`
- `JwtAuthenticationFilter`
- `UserRepository`

Flight:

- `FlightService`
- `FlightRepository`
- `FlightMapper`
- `FlightFareRepository`
- `FlightInventoryRepository`

Booking:

- `BookingService`
- `BookingRepository`
- `BookingMapper`
- `BookingController`

AI tools must derive `User` identity from `@AuthenticationPrincipal` or `SecurityContextHolder`. They must never accept a user ID from an LLM request.

## Required Phase 7 Components

Implement in `com.skybook.ai` and appropriate subpackages:

### RAG

- Knowledge Markdown files under `backend/src/main/resources/airline-policies/`
- Policy topics: baggage, booking, cancellation, refunds, check-in, passenger requirements
- Document ingestion and chunking
- Embedding model integration
- PostgreSQL/pgvector vector store where supported
- Retrieval and grounded response fallback
- Idempotent ingestion; do not ingest on every startup unnecessarily

### Tool Calling

Implement read-only tools:

- `searchFlights`
- `getFlightDetails`
- `getMyBookings`
- `getBookingDetails`

Tools must reuse existing services.

Each tool must validate inputs and enforce ownership through server-side security context.

### Chat Memory

- Use Spring AI `JdbcChatMemoryRepository` / `ChatMemory` / `MessageChatMemoryAdvisor` APIs when compatible
- Initialize PostgreSQL schema through Spring AI configuration where feasible
- Add separate `Conversation` ownership metadata entity or repository if Spring AI framework storage alone cannot enforce ownership
- Generate conversation IDs server-side
- Conversation endpoints:
  - `POST /api/v1/ai/chat`
  - `GET /api/v1/ai/conversations`
  - `GET /api/v1/ai/conversations/{conversationId}`

Ownership must be verified before retrieval or chat continuation.

### MCP

- Implement a minimal MCP server exposing only flight search and flight details
- Implement a runnable client/demo that discovers and invokes at least one tool
- Prefer Spring AI MCP 2.1.0-RC2 only after verifying compatibility with Spring Boot 4.1.2 / Spring AI 2.0.0
- If incompatible, implement a clearly separated read-only MCP-style JSON-RPC HTTP demonstration and classify it honestly, but do not claim native Spring AI MCP without evidence

## Known API Verification Needs

Before compiling RAG/chat code, verify these APIs:

- `ChatClient.builder(ChatModel)`
- `ChatClient.Builder.defaultAdvisors(...)`
- `MessageChatMemoryAdvisor.builder(ChatMemory).build()`
- `JdbcChatMemoryRepository.builder().jdbcTemplate(...).dialect(new PostgresChatMemoryRepositoryDialect()).build()`
- `MessageWindowChatMemory.builder().chatMemoryRepository(...).maxMessages(...).build()`
- `PgVectorStore.builder(JdbcTemplate, EmbeddingModel)`
- `QuestionAnswerAdvisor.builder(VectorStore).build()` or the exact 2.0.0 equivalent
- `ToolCallbacks.from(...)`
- `@Tool` method annotation availability

## MCP Dependency Blocker

There is no confirmed resolved MCP artifact in the local 2.0.0 cache.

Do not silently upgrade to a Spring AI 2.1.0 dependency set incompatible with the project. The next model should:

1. Verify whether MCP starter coordinates exist for 2.0.0.
2. If not, either use direct MCP SDK 1.0.0 compatible artifacts or implement an honest minimal MCP JSON-RPC demo.
3. Document the status accurately.

## Embedding/Gateway Blocker

No live embedding capability was verified.

Do not claim live RAG embedding works without actually calling the gateway.

If the gateway only supports chat, implement a fallback for tests and document live embedding as blocked.

## Current Database

## 2026-11-20 Progress Update

Phase 7 core implementation was completed with minimal-compatible Spring AI 2.0.0 components and additive code changes.

### Implemented

- RAG ingestion and retrieval wiring:
  - Added policy markdown files under `backend/src/main/resources/airline-policies/`.
  - Added `AiKnowledgeIngestionService` startup ingestion with chunking and checksum-based skip tracking.
  - Added `AiPolicyDocumentState` entity/repository for idempotency metadata.
  - Added `RetrievalAugmentationAdvisor` wiring in `AiConfig` (conditional on vector store presence).
- Tool Calling (read-only only):
  - Added `AeroMateTools` with `@Tool` methods:
    - `searchFlights`
    - `getFlightDetails`
    - `getMyBookings`
    - `getBookingDetails`
  - Uses trusted `SecurityContextHolder` user principal for booking tools.
- Persistent Chat Memory and ownership:
  - Added `AiConversation` entity and repository for conversation ownership metadata.
  - Added `AiConversationService` with ownership enforcement.
  - Configured Spring AI JDBC chat memory and message window memory in `AiConfig`.
  - Added endpoints:
    - `POST /api/v1/ai/chat`
    - `GET /api/v1/ai/conversations`
    - `GET /api/v1/ai/conversations/{conversationId}`
- MCP demonstration:
  - Implemented minimal MCP-style JSON-RPC endpoint:
    - `POST /api/v1/mcp/rpc`
    - methods: `tools/list`, `tools/call`
    - tools: `flight.search`, `flight.details`
  - This is explicitly a compatibility fallback and not native Spring AI MCP server/client integration.

### Configuration updates

- `application.properties` now uses env-based AI credentials:
  - `SKYBOOK_OPENAI_BASE_URL`
  - `SKYBOOK_OPENAI_API_KEY`
  - `SKYBOOK_OPENAI_CHAT_MODEL`
- Enabled Spring AI JDBC chat memory schema init and pgvector schema init/validation.

### Validation completed

- `./mvnw.cmd test` -> BUILD SUCCESS
  - Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
- `./mvnw.cmd package -DskipTests` -> BUILD SUCCESS

### Remaining blocker status

- Native Spring AI MCP starter integration remains unresolved for current 2.0.0 dependency compatibility in this workspace.
- Live gateway behavior for embeddings still depends on provider capability and credentials; fallback behavior is implemented (chat can continue without RAG context when retrieval path fails).

PostgreSQL is available locally and Spring AI JDBC memory schema initialization may be enabled through the JDBC starter configuration.

Potential pgvector extension status is unknown. Check `SELECT * FROM pg_available_extensions WHERE name = 'vector';` before assuming it is enabled.

## Testing Status

Current tests: 10, passing, no failures/errors.

No Spring AI tests exist yet.

Add mocked tests for:

- RAG document retrieval
- tool callbacks
- authenticated booking tool ownership
- conversation ownership
- chat memory persistence repository
- chat controller behavior
- MCP discovery/invocation

Do not depend on live gateway credentials in automated tests.

## Build Commands

```powershell
cd c:\Users\rsantosh\OneDrive - Capgemini\Documents\GitHub\SkyBook\backend
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

## Immediate Next Tasks

1. Read the current POM and remove/replace duplicate or incorrect Spring AI dependency entries if needed.
2. Compile the project to expose missing classes/APIs.
3. Implement conversation ownership entities/repositories.
4. Implement RAG ingestion and retrieval.
5. Implement tools and chat REST API.
6. Implement MCP using verified compatible APIs.
7. Add tests.
8. Update `docs/AI_INTEGRATION.md` and `docs/TESTING.md`.
9. Run clean test/package and live gateway checks only if credentials work.

## Security Requirements

- AI endpoints require authentication.
- Conversation IDs are generated server-side or validated against ownership metadata.
- Customers cannot access other users' conversations or bookings.
- Tools are read-only.
- No API key appears in logs or documentation.
- The existing JWT principal is the source of identity.

## Stop Conditions

Pause and report rather than guessing if:

- Spring AI 2.0.0 and the gateway are incompatible.
- Embeddings are unsupported by the gateway.
- MCP starters cannot be resolved or used with the existing Boot version.
- pgvector is unavailable and no compatible fallback is approved.
