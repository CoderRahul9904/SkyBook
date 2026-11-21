# AI Integration (Phase 7) - AeroMate

## Scope Implemented

Phase 7 AI integration is implemented in the existing backend with:

1. RAG (policy knowledge retrieval)
2. Spring AI Tool Calling (read-only tools)
3. Persistent JDBC Chat Memory with ownership enforcement
4. Minimal MCP-style JSON-RPC demonstration (compatible fallback for current dependency set)

## Architecture

AI module location:

- `backend/src/main/java/com/skybook/ai`

Main components:

- `AiController`: REST APIs for chat and conversations
- `AeroMateService`: orchestrates ChatClient, advisors, and tool callbacks
- `AeroMateTools`: read-only tool methods reusing existing services
- `AiConversationService`: ownership checks and conversation metadata lifecycle
- `AiKnowledgeIngestionService`: markdown ingestion, chunking, vector-store population
- `McpController`: MCP-style JSON-RPC tool discovery and invocation demo

## REST Endpoints

### AeroMate APIs

- `POST /api/v1/ai/chat`
- `GET /api/v1/ai/conversations`
- `GET /api/v1/ai/conversations/{conversationId}`

Behavior:

- Authenticated only (existing JWT security rules apply)
- Conversation ownership enforced by authenticated user id
- Chat memory persisted via Spring AI JDBC Chat Memory repository
- Tool execution is read-only and uses trusted Spring Security context

### MCP-style Demo API

- `POST /api/v1/mcp/rpc`

JSON-RPC methods:

- `tools/list`
- `tools/call` with:
  - `flight.search`
  - `flight.details`

This is an MCP-style fallback implementation because native Spring AI MCP artifacts were not confirmed compatible in the current Spring AI 2.0.0 setup.

## RAG Design

Policy documents are under:

- `backend/src/main/resources/airline-policies/`

Implemented topics:

- baggage allowance
- booking guidelines
- cancellation policy
- refund information
- check-in guidelines
- passenger requirements

Ingestion flow:

1. On application startup, markdown files are loaded.
2. Files are chunked with Spring AI text splitter.
3. Chunks are embedded and inserted into pgvector-backed vector store (when available).
4. Document checksums are tracked in `ai_policy_document_state` to avoid unnecessary reinsertion.

Retrieval flow:

- `RetrievalAugmentationAdvisor` is applied when vector store is available.
- If RAG retrieval fails at runtime (for example embedding/provider limits), chat falls back to memory + tools and continues responding.

## Tool Calling and Security Boundaries

Read-only tools:

- `searchFlights`
- `getFlightDetails`
- `getMyBookings`
- `getBookingDetails`

Security guarantees:

- User identity is resolved from Spring Security context only.
- No tool accepts user id from model input.
- Booking ownership validation remains in existing booking service.
- No mutation tools are exposed.

## Persistent Chat Memory

- Uses Spring AI JDBC memory repository with PostgreSQL schema initialization enabled.
- Conversations are tracked in `ai_conversations` with `conversation_id` and `owner_user_id`.
- Message history is persisted by Spring AI; ownership metadata is stored separately for authorization.

## Configuration

Updated application properties use environment variables:

- `SKYBOOK_OPENAI_BASE_URL`
- `SKYBOOK_OPENAI_API_KEY`
- `SKYBOOK_OPENAI_CHAT_MODEL`

Key AI settings:

- JDBC chat memory schema initialization enabled
- pgvector schema initialization and validation enabled

## Run Commands

From `backend/`:

1. `set SKYBOOK_OPENAI_API_KEY=<your-key>` (Windows PowerShell: `$env:SKYBOOK_OPENAI_API_KEY="..."`)
2. `./mvnw.cmd spring-boot:run`

## Quick Verification

1. Login and obtain JWT from `/api/v1/auth/login`.
2. Call `POST /api/v1/ai/chat` with Authorization header.
3. Reuse returned conversation id in another chat call.
4. Call `GET /api/v1/ai/conversations` and `GET /api/v1/ai/conversations/{conversationId}`.
5. Call `POST /api/v1/mcp/rpc` with `tools/list` and then `tools/call`.

## Known Limitations

- Native Spring AI MCP server/client integration is not included due unresolved compatibility in this dependency set.
- Live gateway/embedding behavior depends on provider capabilities and credentials.
