# SkyBook MVP Database Design (PostgreSQL)

Document status: Design baseline prepared from draft source artifacts and explicit user approval to proceed. Not automatically marked as final approved architecture.

## 1. Database Overview

This document defines the PostgreSQL schema design for the SkyBook MVP in alignment with:
- Project objective and constraints from AGENTS.md and PROJECT_PROPOSAL.md
- Functional requirements captured in the SRS
- Domain model in the class diagram
- Entities and relationships in the ER diagram

MVP design principles used:
- Keep schema simple and demonstrable for a four-day build.
- Preserve transactional correctness for booking and cancellation.
- Enforce core business rules through database constraints where practical.
- Keep AI persistence concerns isolated from core booking tables.
- Do not introduce entities that are outside the ERD baseline.

### 1.1 ERD Scope Validation

Proceeding is based on explicit user approval in this session. The source ERD is still marked Draft in its own file, so this document is a production-oriented design baseline that still needs owner sign-off as final.

### 1.2 Included Entity Set

Included tables (from ERD):
- users
- airports
- flights
- flight_inventory
- flight_fare
- bookings
- booking_passengers
- conversations
- chat_messages
- policy_documents
- policy_chunks

### 1.3 Roles and Airlines Handling

- Roles: Implemented as a constrained column in users.role (CUSTOMER, ADMIN) to match ERD and MVP simplicity.
- Airlines: No separate airlines table in approved ERD baseline. Airline attributes remain in flights as airline_name and airline_code.

## 2. Schema Conventions

- Primary keys: bigint generated identity.
- Timestamps: timestamptz in UTC.
- Monetary values: numeric with fixed precision and scale.
- Enumerations: varchar with CHECK constraints for MVP portability.
- Soft lifecycle for bookings: status transition CONFIRMED -> CANCELLED; no physical delete required for operational flow.

## 3. Table-by-Table Specification

## 3.1 users

Purpose: Authentication owner entity for CUSTOMER and ADMIN users.

| Column | Type | Null | Default | Constraints | Notes |
|---|---|---|---|---|---|
| id | bigint | NOT NULL | generated always as identity | PK | Surrogate key |
| email | varchar(320) | NOT NULL | none | UNIQUE | Login identity |
| password_hash | varchar(255) | NOT NULL | none | none | One-way hash only |
| full_name | varchar(120) | NOT NULL | none | none | Display and booking ownership context |
| role | varchar(20) | NOT NULL | CUSTOMER | CHECK role in (CUSTOMER, ADMIN) | Server-controlled authorization role |
| created_at | timestamptz | NOT NULL | now() | none | Audit timestamp |

Relationships:
- users.id -> bookings.customer_id (1 to many)
- users.id -> conversations.owner_user_id (1 to many)

Authentication and validation notes:
- Password hashes must be generated with BCrypt.
- Registration password policy requires at least 8 characters with uppercase, lowercase, digit and special character constraints.
- JWT access-token expiry is fixed to 60 minutes for MVP, with no refresh-token flow.

Important indexes:
- unique index on email
- optional non-unique index on role for admin listing filters

## 3.2 airports

Purpose: Master data for origin and destination airports.

| Column | Type | Null | Default | Constraints | Notes |
|---|---|---|---|---|---|
| id | bigint | NOT NULL | generated always as identity | PK | Surrogate key |
| code | varchar(10) | NOT NULL | none | UNIQUE | Airport code, case-normalized by app |
| name | varchar(120) | NOT NULL | none | none | Airport name |
| city | varchar(120) | NOT NULL | none | none | City |
| country | varchar(120) | NOT NULL | none | none | Country |
| created_at | timestamptz | NOT NULL | now() | none | Audit timestamp |
| updated_at | timestamptz | NOT NULL | now() | none | Audit timestamp |

Relationships:
- airports.id -> flights.origin_airport_id
- airports.id -> flights.destination_airport_id

Important indexes:
- unique index on code
- optional index on city for admin search

## 3.3 flights

Purpose: Flight schedule and operating identity for one-way direct flights in MVP.

| Column | Type | Null | Default | Constraints | Notes |
|---|---|---|---|---|---|
| id | bigint | NOT NULL | generated always as identity | PK | Surrogate key |
| airline_name | varchar(120) | NOT NULL | none | none | Stored directly in flight row |
| airline_code | varchar(10) | NOT NULL | none | none | Stored directly in flight row |
| flight_number | varchar(20) | NOT NULL | none | none | Airline flight number |
| origin_airport_id | bigint | NOT NULL | none | FK -> airports(id) | Origin airport |
| destination_airport_id | bigint | NOT NULL | none | FK -> airports(id) | Destination airport |
| departure_time | timestamptz | NOT NULL | none | none | Scheduled departure |
| arrival_time | timestamptz | NOT NULL | none | none | Scheduled arrival |
| status | varchar(20) | NOT NULL | SCHEDULED | CHECK status in (SCHEDULED, UPDATED, CANCELLED) | Flight status |
| created_at | timestamptz | NOT NULL | now() | none | Audit timestamp |
| updated_at | timestamptz | NOT NULL | now() | none | Audit timestamp |

Table constraints:
- CHECK (origin_airport_id <> destination_airport_id)
- CHECK (arrival_time > departure_time)

Relationships:
- flights.id -> flight_inventory.flight_id
- flights.id -> flight_fare.flight_id
- flights.id -> bookings.flight_id

Important indexes:
- composite index on (origin_airport_id, destination_airport_id, departure_time)
- composite index on (departure_time, status)
- optional unique index on (airline_code, flight_number, departure_time)

## 3.4 flight_inventory

Purpose: Cabin-specific seat inventory per flight.

| Column | Type | Null | Default | Constraints | Notes |
|---|---|---|---|---|---|
| id | bigint | NOT NULL | generated always as identity | PK | Surrogate key |
| flight_id | bigint | NOT NULL | none | FK -> flights(id) | Flight reference |
| cabin_class | varchar(30) | NOT NULL | none | CHECK cabin_class in (ECONOMY, BUSINESS) | Cabin discriminator |
| total_seats | integer | NOT NULL | none | CHECK total_seats >= 0 | Capacity |
| available_seats | integer | NOT NULL | none | CHECK available_seats >= 0 | Mutable availability |
| updated_at | timestamptz | NOT NULL | now() | none | Audit timestamp |

Table constraints:
- UNIQUE (flight_id, cabin_class)
- CHECK (available_seats <= total_seats)

Relationships:
- many inventory rows per flight, one per cabin class

Important indexes:
- unique index on (flight_id, cabin_class)
- optional index on (cabin_class, available_seats)

Concurrency note:
- Booking seat deduction must lock target row by (flight_id, cabin_class), typically via SELECT FOR UPDATE.

## 3.5 flight_fare

Purpose: Cabin-specific fare per flight.

| Column | Type | Null | Default | Constraints | Notes |
|---|---|---|---|---|---|
| id | bigint | NOT NULL | generated always as identity | PK | Surrogate key |
| flight_id | bigint | NOT NULL | none | FK -> flights(id) | Flight reference |
| cabin_class | varchar(30) | NOT NULL | none | CHECK cabin_class in (ECONOMY, BUSINESS) | Cabin discriminator |
| amount | numeric(12,2) | NOT NULL | none | CHECK amount >= 0 | Exact money representation |
| updated_at | timestamptz | NOT NULL | now() | none | Audit timestamp |

Table constraints:
- UNIQUE (flight_id, cabin_class)

Relationships:
- many fare rows per flight, one per cabin class

Important indexes:
- unique index on (flight_id, cabin_class)

## 3.6 bookings

Purpose: Booking header with ownership, PNR, fare snapshot, and lifecycle state.

| Column | Type | Null | Default | Constraints | Notes |
|---|---|---|---|---|---|
| id | bigint | NOT NULL | generated always as identity | PK | Surrogate key |
| pnr | varchar(12) | NOT NULL | none | UNIQUE | Global booking reference |
| customer_id | bigint | NOT NULL | none | FK -> users(id) | Booking owner |
| flight_id | bigint | NOT NULL | none | FK -> flights(id) | Booked flight |
| cabin_class | varchar(30) | NOT NULL | none | CHECK cabin_class in (ECONOMY, BUSINESS) | Required for restore path |
| passenger_count | integer | NOT NULL | none | CHECK passenger_count >= 1 | Also used for inventory restore |
| fare_snapshot | numeric(12,2) | NOT NULL | none | CHECK fare_snapshot >= 0 | Backend-computed total |
| status | varchar(20) | NOT NULL | CONFIRMED | CHECK status in (CONFIRMED, CANCELLED) | Booking lifecycle |
| created_at | timestamptz | NOT NULL | now() | none | Audit timestamp |
| updated_at | timestamptz | NOT NULL | now() | none | Audit timestamp |

Relationships:
- bookings.id -> booking_passengers.booking_id
- bookings.flight_id with bookings.cabin_class maps cancellation restore target to flight_inventory

Important indexes:
- unique index on pnr
- index on customer_id and created_at desc
- index on flight_id
- optional composite index on (customer_id, status, created_at desc)

## 3.7 booking_passengers

Purpose: Passenger rows tied to one booking.

| Column | Type | Null | Default | Constraints | Notes |
|---|---|---|---|---|---|
| id | bigint | NOT NULL | generated always as identity | PK | Surrogate key |
| booking_id | bigint | NOT NULL | none | FK -> bookings(id) | Parent booking |
| full_name | varchar(120) | NOT NULL | none | none | Passenger full name |
| birth_date | date | NOT NULL | none | none | Passenger date of birth |

Relationships:
- many passengers per booking

Important indexes:
- index on booking_id

Integrity rule implementation note:
- passenger_count match with child row count should be validated in booking transaction/service logic. It can be additionally protected via deferred trigger in PostgreSQL if strict DB enforcement is required.

## 3.8 conversations

Purpose: Ownership metadata for AI chat memory sessions.

| Column | Type | Null | Default | Constraints | Notes |
|---|---|---|---|---|---|
| id | bigint | NOT NULL | generated always as identity | PK | Surrogate key |
| conversation_id | varchar(100) | NOT NULL | none | UNIQUE | External chat context key |
| owner_user_id | bigint | NOT NULL | none | FK -> users(id) | Authenticated owner |
| created_at | timestamptz | NOT NULL | now() | none | Audit timestamp |
| updated_at | timestamptz | NOT NULL | now() | none | Audit timestamp |

Relationships:
- conversations.owner_user_id -> users.id
- conversations.conversation_id referenced by chat_messages.conversation_id

Important indexes:
- unique index on conversation_id
- composite index on (owner_user_id, updated_at desc)

Security rule:
- Conversation access must always include owner_user_id = authenticated user id predicate.

## 3.9 chat_messages

Purpose: Persistent chat memory rows associated with conversation_id.

| Column | Type | Null | Default | Constraints | Notes |
|---|---|---|---|---|---|
| id | bigint | NOT NULL | generated always as identity | PK | Surrogate key |
| conversation_id | varchar(100) | NOT NULL | none | FK -> conversations(conversation_id) | Conversation key |
| role | varchar(20) | NOT NULL | none | CHECK role in (USER, ASSISTANT, SYSTEM, TOOL) | Role vocabulary may vary by Spring AI version |
| content | text | NOT NULL | none | none | Message body |
| created_at | timestamptz | NOT NULL | now() | none | Message timestamp |

Relationships:
- many chat messages per conversation

Important indexes:
- composite index on (conversation_id, created_at)

Compatibility note:
- If Spring AI auto-creates chat-memory tables with different naming/shape, map this conceptual design to framework tables and keep conversations ownership metadata table unchanged.

## 3.10 policy_documents

Purpose: Stores approved RAG source documents.

| Column | Type | Null | Default | Constraints | Notes |
|---|---|---|---|---|---|
| id | bigint | NOT NULL | generated always as identity | PK | Surrogate key |
| title | varchar(200) | NOT NULL | none | none | Logical document title |
| source | varchar(500) | NOT NULL | none | none | Source identifier/path |
| content | text | NOT NULL | none | none | Canonical document text |
| created_at | timestamptz | NOT NULL | now() | none | Audit timestamp |
| updated_at | timestamptz | NOT NULL | now() | none | Audit timestamp |

Relationships:
- policy_documents.id -> policy_chunks.policy_document_id

Important indexes:
- optional unique index on source

## 3.11 policy_chunks

Purpose: Chunked policy content and vector embeddings for RAG retrieval.

| Column | Type | Null | Default | Constraints | Notes |
|---|---|---|---|---|---|
| id | bigint | NOT NULL | generated always as identity | PK | Surrogate key |
| policy_document_id | bigint | NOT NULL | none | FK -> policy_documents(id) | Source provenance |
| chunk_index | integer | NOT NULL | none | CHECK chunk_index >= 0 | Chunk order |
| content | text | NOT NULL | none | none | Chunk body |
| embedding | vector | NULL | none | none | pgvector embedding; nullable until generated |
| created_at | timestamptz | NOT NULL | now() | none | Audit timestamp |

Table constraints:
- UNIQUE (policy_document_id, chunk_index)

Relationships:
- many chunks per document

Important indexes:
- unique index on (policy_document_id, chunk_index)
- vector index for similarity search when pgvector is enabled (ivfflat or hnsw based on version and performance tests)

Fallback note:
- If pgvector is unavailable, use Spring AI compatible vector store fallback and keep policy_documents/policy_chunks for source and chunk traceability.

## 4. Relationship and Constraint Summary

Core cardinalities:
- users 1 to many bookings
- users 1 to many conversations
- airports 1 to many flights as origin and destination
- flights 1 to many flight_inventory
- flights 1 to many flight_fare
- flights 1 to many bookings
- bookings 1 to many booking_passengers
- conversations 1 to many chat_messages
- policy_documents 1 to many policy_chunks

Critical business rule enforcement map:
- PNR uniqueness: unique index on bookings.pnr
- Email uniqueness: unique index on users.email
- Airport code uniqueness: unique index on airports.code
- Unique cabin fare and inventory rows per flight: unique (flight_id, cabin_class) on flight_fare and flight_inventory
- Seat bounds: checks on flight_inventory ensure available_seats between 0 and total_seats
- Booking stores cabin and passenger count: bookings.cabin_class and bookings.passenger_count not null
- Monetary exactness: numeric columns for fares and fare_snapshot
- Conversation ownership: conversations.owner_user_id foreign key and ownership checks in application queries

## 5. Indexing Strategy

Primary strategy:
- Use PK and unique indexes for identity and domain uniqueness.
- Add read-path indexes for top user stories: flight search, booking history, PNR lookup, conversation retrieval.

Recommended non-PK indexes:
- flights(origin_airport_id, destination_airport_id, departure_time)
- flights(departure_time, status)
- bookings(customer_id, created_at desc)
- bookings(flight_id)
- conversations(owner_user_id, updated_at desc)
- chat_messages(conversation_id, created_at)

Vector search index strategy:
- pgvector with index type selected after dimensionality and recall/latency benchmark.
- Keep chunk table modest for MVP and reindex only after larger document ingestion.

## 6. Booking and Cancellation Transaction Design

## 6.1 Booking transaction flow

Single transaction boundary:
1. Validate authenticated customer context.
2. Validate flight exists, status is eligible, and fare row exists for selected cabin.
3. Lock target inventory row by flight_id and cabin_class.
4. Recheck available seats >= passenger_count under lock.
5. Compute total fare in backend using flight_fare.amount * passenger_count.
6. Insert bookings row with generated unique PNR and fare snapshot.
7. Insert booking_passengers rows.
8. Decrease flight_inventory.available_seats by passenger_count.
9. Commit.

Failure behavior:
- Any failure rolls back all steps, including seat deduction and inserted rows.

Concurrency controls:
- Row-level lock on inventory row prevents oversell race conditions.
- Optionally combine with retry logic on lock contention or unique-PNR conflicts.

## 6.2 Cancellation transaction flow

Single transaction boundary:
1. Fetch booking by pnr with owner validation.
2. Validate status is CONFIRMED and time cutoff is satisfied.
3. Lock matching inventory row by booking.flight_id and booking.cabin_class.
4. Set booking status to CANCELLED.
5. Increase available_seats by booking.passenger_count.
6. Validate new available_seats <= total_seats (guarded by check constraint).
7. Commit.

Exactly-once inventory restore:
- Repeated cancellation attempts are blocked by booking status check.
- Since status update and seat restore are in one transaction, partial updates are avoided.

## 7. Data Integrity and Security Considerations

Integrity controls:
- Strong FK network to prevent orphan records.
- CHECK constraints for enum-like values and numeric bounds.
- Unique constraints on identity and business keys.
- Default timestamps and immutable ownership references in application layer.

Security controls:
- Store password hashes only.
- Derive booking and conversation ownership from authenticated user context, never client-provided user id.
- Scope all customer reads with ownership predicates.
- Restrict admin operations through role checks.
- Apply least-privilege DB credentials for app runtime.

Operational controls:
- Avoid destructive ddl-auto modes in normal environments.
- Prefer migration-based changes (for example Flyway or Liquibase).
- Enable audit logging and slow query monitoring for booking-critical operations.

## 8. Spring AI Database Integration Notes

## 8.1 Chat Memory Integration

- Use Spring AI JDBC chat memory for persistent messages.
- Keep conversations table as ownership and access-control anchor.
- Enforce ownership before reading or appending chat history.
- If framework-managed chat table schema differs, treat chat_messages specification as conceptual mapping while preserving the ownership model.

## 8.2 RAG and Vector Storage

- policy_documents and policy_chunks preserve traceable ingestion lineage.
- Generate embeddings per chunk and persist in embedding column where pgvector is available.
- Use similarity search over chunks for retrieval augmentation.
- Keep RAG knowledge base isolated from booking and conversation ownership data.

## 8.3 MCP Impact

- No additional persistent entities are required for MVP MCP demo.
- MCP tools reuse existing flight and booking read models with existing authorization and validations.

## 9. Remaining Design Decisions Requiring Owner Approval

1. Confirm final PNR format and length policy (example: 6 to 8 uppercase alphanumeric) and generator strategy.
2. Confirm numeric precision and scale policy for future international fare ranges (currently numeric(12,2)).
3. Confirm Spring AI version-specific chat-memory table mapping and whether custom chat_messages table is needed or framework tables are used directly.
4. Confirm pgvector extension availability in target environments and final embedding dimension.
5. Confirm whether optional indexes should be included in MVP day-1 migrations or deferred after performance observation.

## 10. Concise Verification Summary

- Schema covers all required domains: users and roles, airports, flights and schedules, cabin fares and inventory, bookings and booking passengers, conversation ownership and chat memory, and RAG/vector integration.
- Airlines are modeled inside flights as approved in ERD baseline; no new airline entity introduced.
- Core business rules are enforced with unique constraints, check constraints, and transaction design.
- Design remains aligned to MVP simplicity and four-day implementation limits.
- Final production approval is pending explicit sign-off of open decisions above.
