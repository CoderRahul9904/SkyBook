# Activity Diagram — AeroMate AI Interaction

## PlantUML Activity Diagram

```plantuml
@startuml
|Customer|
start
:Submit question and conversation ID;

|React Frontend|
:Send chat request;

|JWT Security Context|
:Derive authenticated customer identity;

|AeroMateChatService|
if (Customer authenticated?) then (yes)
  :Validate conversation ownership;

  if (Conversation belongs to customer?) then (yes)
    :Retrieve JDBC chat history;

    if (Question is policy-related?) then (yes)
      |RagService / VectorStoreAdapter|
      :Retrieve relevant approved policy chunks;

      if (Relevant knowledge found?) then (yes)
        :Supply grounded policy context;
      else (no)
        :Mark knowledge unavailable;
      endif
    else (no)
      :No RAG context required;
    endif

    if (Question requires application data?) then (yes)
      |AeroMateToolRegistry|
      :Determine permitted read-only tool;

      if (Tool is flight search or flight details?) then (yes)
        |FlightService|
        :Search flights or retrieve flight details;
      elseif (Tool is my bookings or PNR lookup?) then (yes)
        |BookingService|
        :Retrieve only authenticated customer's bookings;
      else (no)
        :No permitted tool invoked;
      endif

      if (Tool service succeeds?) then (yes)
        :Return validated tool result;
      else (no)
        :Return tool failure without bypassing authorization;
      endif
    else (no)
      :No tool invocation required;
    endif

    |AeroMateChatService|
    :Prepare history, context and tool results;
    :Generate response through Gemini AI Gateway;

    if (LLM or AI Gateway available?) then (yes)
      if (Relevant knowledge was unavailable
      and no permitted data was found?) then (yes)
        :State that the knowledge base
        has insufficient information;
      else (no)
        :Generate grounded assistant response;
      endif

      :Persist customer and assistant messages;
      |JDBC Chat Memory / PostgreSQL|
      :Append persisted conversation history;
      |React Frontend|
      :Display answer and conversation ID;
    else (no)
      :Handle AI service failure gracefully;
      |React Frontend|
      :Display unavailable/error state;
    endif
  else (no)
    |React Frontend|
    :Reject unauthorized conversation access;
  endif
else (no)
  |React Frontend|
  :Reject unauthenticated chat request;
endif

|Customer|
stop
@enduml
```

## Workflow Explanation

The Customer submits a question with a conversation identifier. The chat service verifies authentication and conversation ownership, retrieves persisted JDBC history and decides whether RAG or a permitted read-only tool is needed. Policy retrieval uses approved knowledge chunks; application data is delegated to existing flight or booking services. The LLM returns a grounded response, which is persisted with the conversation history.

## Important Business Rules and Decision Points

- Conversation ownership must match the authenticated user.
- LLM-supplied user identity is never trusted.
- RAG is used only for policy or knowledge-grounded questions.
- Unavailable knowledge causes an insufficient-information acknowledgment instead of invented policy.
- Permitted tools are read-only: flight search, flight details, own bookings and accessible PNR lookup.
- AeroMate cannot create/cancel bookings, process payments or modify fares/inventory.
- Tool failures are handled without bypassing normal authorization.
- Conversation history remains separated from policy-vector data.

## Unresolved Assumptions

- Conversation-ID generation/format remains open.
- pgvector fallback choice remains open.
- MCP transport remains open and is not part of this normal chat workflow.
- Whether RAG source citations are mandatory remains open.
- Maximum memory/history retention remains open.

## Consistency Verification

This activity follows the AeroMate chat sequence and SRS FR-AI-001 through FR-AI-021. MCP is kept as a separate read-only flight interoperability demonstration, consistent with FR-AI-022 through FR-AI-025.
