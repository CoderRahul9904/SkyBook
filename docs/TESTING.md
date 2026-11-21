# Testing Report

## Commands Executed

From `backend/`:

1. `./mvnw.cmd test`
2. `./mvnw.cmd package -DskipTests`

## Actual Results

### Test Run

- Build status: SUCCESS
- Total tests: 21
- Failures: 0
- Errors: 0
- Skipped: 0

AI-related tests added and passed:

- `AiConversationServiceTest`
- `AeroMateToolsTest`
- `AiKnowledgeIngestionServiceTest`
- `McpControllerTest`

### Package Build

- Build status: SUCCESS
- Artifact generated:
  - `backend/target/backend-0.0.1-SNAPSHOT.jar`

## Coverage Notes

Validated in automated tests:

- Conversation ownership checks and auth requirement at service layer
- Tool-calling read-only behaviors and security-context enforcement
- Policy ingestion path and vector-store add invocation
- MCP-style tool discovery and tool invocation routes

Not fully automated (manual/live verification required):

- End-to-end chat against live AI gateway
- Live embedding support and quality with provider-specific model availability
- End-to-end RAG answer grounding quality in real runtime environment

## Manual Verification Checklist

1. Start backend with valid AI gateway API key.
2. Authenticate and call `POST /api/v1/ai/chat` with a policy question.
3. Continue same conversation id in a second chat call and verify context continuity.
4. Verify `GET /api/v1/ai/conversations` returns owned conversations only.
5. Verify `GET /api/v1/ai/conversations/{id}` denies non-owner access.
6. Verify `POST /api/v1/mcp/rpc`:
   - `tools/list` returns `flight.search` and `flight.details`
   - `tools/call` invokes both tools successfully
