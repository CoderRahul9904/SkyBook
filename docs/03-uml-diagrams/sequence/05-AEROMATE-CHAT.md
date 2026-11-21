# Sequence Diagram — AeroMate AI Chat

## Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    actor Customer
    participant UI as React Frontend
    participant Controller as AIChatController
    participant Security as JWT Security Context
    participant ChatService as AeroMateChatService
    participant Memory as JDBC Chat Memory
    participant ConversationRepo as ConversationRepository
    participant DB as PostgreSQL
    participant RAG as RagService
    participant Vector as VectorStoreAdapter
    participant Tools as AeroMateToolRegistry
    participant FlightService as FlightService
    participant BookingService as BookingService
    participant LLM as Gemini through AI Gateway

    Customer->>UI: Send chat message with conversation ID
    UI->>Controller: POST /api/v1/ai/chat
    Controller->>Security: Get authenticated customer identity
    Security-->>Controller: Authenticated user ID

    alt Missing authentication or conversation not owned
        Controller-->>UI: Unauthorized response
        UI-->>Customer: Conversation access denied
    else Owned conversation
        Controller->>ChatService: chat(conversationId, message, userId)
        ChatService->>ConversationRepo: Validate conversation owner
        ConversationRepo->>DB: Read conversation ownership metadata
        DB-->>ConversationRepo: Ownership record

        alt Unknown or unauthorized conversation
            ConversationRepo-->>ChatService: Ownership failure
            ChatService-->>Controller: Access rejected
        else Owned conversation
            ChatService->>Memory: Retrieve conversation history
            Memory->>DB: Read JDBC chat messages
            DB-->>Memory: Previous messages
            Memory-->>ChatService: Conversation context

            opt Message is policy-related
                ChatService->>RAG: retrieveRelevantContext(message)
                RAG->>Vector: Similarity search approved policy chunks
                Vector->>DB: Query vector store

                alt Relevant context found
                    DB-->>Vector: Policy chunks
                    Vector-->>RAG: Relevant chunks
                    RAG-->>ChatService: Grounded policy context
                else No relevant context
                    RAG-->>ChatService: Insufficient knowledge context
                end
            end

            opt Message requires flight or booking data
                ChatService->>Tools: Resolve appropriate read-only tool

                alt Flight search or flight detail requested
                    Tools->>FlightService: search(criteria) or getFlightDetails(id)
                    FlightService-->>Tools: Validated flight information
                else My bookings or PNR details requested
                    Tools->>BookingService: getMyBookings(userId) or getBookingByPnr(userId,pnr)
                    BookingService-->>Tools: Owned booking information or access failure
                end

                Tools-->>ChatService: Tool result
            end

            ChatService->>LLM: Generate response with history,<br/>policy context and tool results

            alt AI Gateway or model failure
                LLM-->>ChatService: Service failure
                ChatService-->>Controller: AI unavailable response
                Controller-->>UI: Error state
            else Response generated
                LLM-->>ChatService: Grounded assistant response
                ChatService->>Memory: Append customer and assistant messages
                Memory->>DB: Persist JDBC chat messages
                DB-->>Memory: Messages persisted
                ChatService-->>Controller: Answer and conversation ID
                Controller-->>UI: Chat response
                UI-->>Customer: Display AeroMate answer
            end
        end
    end
```

## Workflow Explanation

A customer sends an authenticated chat request. `AeroMateChatService` validates server-maintained conversation ownership and retrieves JDBC chat history. Policy-related questions optionally invoke RAG and vector retrieval. Application-data questions invoke only the approved read-only Tool Registry, with customer identity supplied by the security context. The LLM receives conversation history, relevant context and tool results, then returns the answer. Successful exchanges are persisted to JDBC chat memory.

AeroMate tools do not create or cancel bookings or change fares or inventory. If policy retrieval has no relevant information, the response acknowledges insufficient knowledge rather than inventing an answer.

## Consistency and Assumptions

- Uses the approved `/api/v1/ai/chat` operation.
- Conversation ownership metadata is separate from framework-managed messages.
- MCP is a separate interoperability demonstration and is not required in this normal chat sequence.
- pgvector is preferred, with a compatible fallback permitted if unavailable.
- Conversation-ID generation and exact MCP transport remain open decisions.
