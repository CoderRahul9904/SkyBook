# SkyBook — Software Requirements Specification (SRS)

**Document status:** Draft — pending review
**Project:** SkyBook — AI-Assisted Airline Ticket Booking Platform
**Source of truth:** `docs/PROJECT_PROPOSAL.md`
**Architecture:** Modular monolith
**Primary stack:** Java, Spring Boot, React, TypeScript, PostgreSQL, Spring AI
**Implementation constraint:** Four days

> This document is a requirements specification only. It does not authorize implementation, API design, database schema design, or source-code generation beyond this SRS.

## Document Control

| Item | Value |
|---|---|
| Document owner | SkyBook project team |
| Prepared role | Senior Business Analyst / Software Architect |
| Approved status | Not approved |
| Source baseline | Finalized project proposal |
| Scope baseline | MVP only |

## 1. Introduction

### 1.1 Purpose

This Software Requirements Specification defines the functional and non-functional requirements for SkyBook, a full-stack airline ticket booking platform with an AI travel assistant named AeroMate. It is intended for developers, testers, architects, reviewers, and project stakeholders.

### 1.2 Background

Traditional booking systems require users to navigate separate interfaces for flight discovery, passenger entry, booking confirmation, booking management, and policy information. SkyBook provides a single customer and administrative experience backed by transactional business operations and controlled AI assistance.

### 1.3 Intended Audience

- Backend and frontend developers
- Database and security engineers
- Test engineers
- Project reviewers and trainers
- Administrative and customer-product stakeholders

### 1.4 Definitions

| Term | Definition |
|---|---|
| AeroMate | Spring AI-powered conversational airline assistant |
| Admin | Authenticated user authorized for administrative operations |
| Cabin class | Fare and inventory category selected for a flight and booking |
| Customer | Authenticated user with the CUSTOMER role |
| Direct flight | A one-way flight with no intermediate stops in the MVP |
| Flight inventory | Total and currently available seats for a flight by cabin class |
| PNR | Unique Passenger Name Record identifying a booking |
| RAG | Retrieval-Augmented Generation |
| MCP | Model Context Protocol |
| MVP | Minimum viable product scoped to the four-day implementation plan |

### 1.5 References

- `docs/PROJECT_PROPOSAL.md`
- Spring Boot and Spring AI documentation selected for project-compatible dependency versions
- PostgreSQL documentation

## 2. Overall Description

### 2.1 Product Perspective

SkyBook is a modular monolithic web application with a separate React frontend and Spring Boot backend. PostgreSQL is the system of record for application and AI conversational data. AeroMate is an integrated Spring AI capability and must not replace backend authorization or mutation logic.

### 2.2 Project Scope

The MVP includes:

- Customer registration and JWT authentication
- Direct, one-way flight search and flight selection
- Passenger entry and transactional booking creation
- PNR generation and fare calculation
- Booking history, PNR lookup, and cancellation
- Airport and flight administration
- Fare and cabin inventory administration
- AeroMate RAG, read-only Tool Calling, JDBC Chat Memory, and MCP demonstration
- Swagger/OpenAPI documentation and test execution evidence

### 2.3 Project Objectives

The system shall:

1. Provide accurate, backend-validated flight search and booking operations.
2. Preserve database consistency and prevent seat overselling.
3. Protect customer data and enforce role-based authorization.
4. Provide grounded policy answers and controlled access to application data through AeroMate.
5. Maintain conversation context for authenticated users.
6. Demonstrate all four agreed Spring AI capabilities within the four-day schedule.
7. Produce a maintainable, understandable, and demonstrable MVP.

### 2.4 Project Constraints

- Development timeline is approximately four days.
- No microservices architecture is permitted.
- No real airline/GDS or payment integration is permitted in the MVP.
- PostgreSQL is the primary database.
- Core booking correctness takes precedence over AI enhancements.
- If the preferred pgvector integration is unavailable, a compatible fallback may be used without delaying core booking functionality.

### 2.5 Assumptions

- Flight inventory and schedules are simulated in PostgreSQL.
- The Capgemini AI Gateway and Gemini-compatible endpoint are available for the implementation team.
- A small curated set of approved Markdown policy documents can be supplied for RAG.
- Administrators manage permitted application data through the backend.
- The network and database are available during normal demonstration use.

## 3. User Roles and System Actors

| Actor | Description | Primary responsibilities |
|---|---|---|
| Guest | Unauthenticated visitor | Register, authenticate, and potentially initiate flight search only if search is permitted by implementation |
| Customer | Authenticated `CUSTOMER` | Search flights, create bookings, view/cancel owned bookings, chat with AeroMate |
| Administrator | Authenticated `ADMIN` | Manage airports, flights, fares, inventory, and view bookings |
| AeroMate | AI application component | Retrieve grounded policy context and invoke controlled read-only tools |
| PostgreSQL | Persistence and vector system | Store users, business records, conversational memory metadata, and AI data |
| Capgemini AI Gateway | External model gateway | Provide Gemini model access through an OpenAI-compatible interface |
| MCP Server/Client | Internal demonstration components | Discover and invoke read-only MCP flight tools |

### 3.1 Actor Interaction Summary

- Guest users may register and log in.
- Customer users may use all customer booking functions and AeroMate.
- Admin users may use administrative functions but must not receive additional customer privileges merely by role association.
- AeroMate cannot confirm, cancel, pay, or modify application records; mutations remain in authenticated normal workflows.

## 4. Functional Requirements

### 4.1 Authentication and Authorization Support

| ID | Requirement |
|---|---|
| FR-AUTH-001 | The system shall allow a guest to register a customer account using unique registration details. |
| FR-AUTH-002 | The system shall reject registration with an already registered email address. |
| FR-AUTH-003 | The system shall authenticate registered customers and issue a JWT on successful login. |
| FR-AUTH-004 | The system shall reject invalid credentials without exposing whether a username exists. |
| FR-AUTH-005 | The system shall assign CUSTOMER or ADMIN roles from server-side account data. |
| FR-AUTH-006 | The system shall allow authenticated CUSTOMER users to access customer endpoints. |
| FR-AUTH-007 | The system shall allow only authenticated ADMIN users to access administrative endpoints. |
| FR-AUTH-008 | The system shall derive the user identity for booking and conversation operations from the authenticated security context. |
| FR-AUTH-009 | The system shall not trust user identity, fare, status, inventory, or authorization data supplied only by the client. |
| FR-AUTH-010 | The system shall reject missing or invalid authentication for protected resources with an unauthorized or forbidden response as appropriate. |

### 4.2 Module 1 — Flight Search

| ID | Requirement |
|---|---|
| FR-FLIGHT-001 | The system shall allow the user to search direct, one-way flights using origin airport, destination airport, travel date, passenger count, and cabin class. |
| FR-FLIGHT-002 | The system shall require origin and destination to be different valid airport codes. |
| FR-FLIGHT-003 | The system shall require passenger count to be a positive integer. |
| FR-FLIGHT-004 | The system shall reject search requests containing invalid or unsupported cabin classes. |
| FR-FLIGHT-005 | The system shall reject travel dates that cannot represent a valid, schedulable future flight according to backend validation rules. |
| FR-FLIGHT-006 | The system shall retrieve matching flights from PostgreSQL rather than a real external airline API. |
| FR-FLIGHT-007 | The system shall return only flights that have at least the requested number of available seats in the requested cabin class. |
| FR-FLIGHT-008 | The system shall return an empty result set rather than an application error when valid criteria match no flights. |
| FR-FLIGHT-009 | Each search result shall include airline name, flight number, route, schedule, duration, stops, available seats, and applicable cabin fare. |
| FR-FLIGHT-010 | The search result shall identify all direct flights as having zero stops for the MVP. |

#### Acceptance Criteria — Flight Search

- AC-FLIGHT-01: Valid search criteria return only matching direct flights with sufficient inventory.
- AC-FLIGHT-02: Invalid origin/destination, passenger count, cabin class, or travel date produces a validation response and no search result.
- AC-FLIGHT-03: A valid search with no matches returns an empty list.
- AC-FLIGHT-04: The same search criteria return no flights if available inventory is below passenger count.
- AC-FLIGHT-05: Every displayed result contains the agreed flight, route, fare, and inventory fields.

### 4.3 Module 2 — Flight Selection

| ID | Requirement |
|---|---|
| FR-SELECT-001 | The system shall allow a user to retrieve details for a selected flight by flight identifier. |
| FR-SELECT-002 | Flight detail responses shall include airline name and code, flight number, origin, destination, schedule, duration, stops, inventory, and cabin fare information. |
| FR-SELECT-003 | The system shall allow the user to select a cabin class for booking. |
| FR-SELECT-004 | The system shall revalidate flight existence, status, cabin fare, and inventory availability at or before booking confirmation. |
| FR-SELECT-005 | The system shall reject selection of a flight or cabin combination that no longer satisfies passenger count and availability. |
| FR-SELECT-006 | The frontend shall present loading, success, empty, and error states for flight selection interactions. |

#### Acceptance Criteria — Flight Selection

- AC-SELECT-01: Selecting a valid flight displays complete flight details and requested cabin fare.
- AC-SELECT-02: Selecting an unknown or unavailable flight is rejected with a clear error.
- AC-SELECT-03: Backend booking validation rejects stale or modified fare/inventory assumptions supplied by the frontend.
- AC-SELECT-04: Direct-flight results and details consistently show zero stops.

### 4.4 Module 3 — Passenger and Booking Management

| ID | Requirement |
|---|---|
| FR-BOOKING-001 | The system shall allow an authenticated CUSTOMER to create a booking for a valid flight and cabin class. |
| FR-BOOKING-002 | The booking request shall include flight identifier, cabin class, passenger count, and passenger details. |
| FR-BOOKING-003 | Each passenger shall provide a full name and date of birth. |
| FR-BOOKING-004 | The system shall validate passenger full name, date of birth, passenger count, and passenger-record count on the backend. |
| FR-BOOKING-005 | The system shall calculate the total booking fare from backend flight-fare data. |
| FR-BOOKING-006 | The system shall not trust any fare or total amount supplied by the client. |
| FR-BOOKING-007 | The system shall verify available seat inventory for the requested passenger count before reserving seats. |
| FR-BOOKING-008 | The system shall reserve the required seats transactionally. |
| FR-BOOKING-009 | The system shall create passenger records associated with the new booking. |
| FR-BOOKING-010 | The system shall generate a unique PNR for every successfully confirmed booking. |
| FR-BOOKING-011 | A successful booking response shall include PNR, flight information, cabin class, passenger count, passengers, fare snapshot, status, and timestamps. |
| FR-BOOKING-012 | The booking operation shall roll back inventory, passenger, and booking changes if any required step fails. |
| FR-BOOKING-013 | The system shall prevent a booking operation from reducing available seats below zero or increasing available seats above total seats. |
| FR-BOOKING-014 | The system shall persist the fare snapshot and booking cabin class required for later booking history and inventory restoration. |
| FR-BOOKING-015 | The system shall use database locking or equivalent concurrency controls to prevent overselling under simultaneous booking attempts. |

#### Acceptance Criteria — Passenger and Booking Management

- AC-BOOK-01: A valid request creates one booking and passenger records and reserves exactly the requested number of seats.
- AC-BOOK-02: The PNR is unique across bookings and appears in the booking response.
- AC-BOOK-03: Backend-calculated fare matches the applicable flight fare multiplied by passenger count, with no client-supplied fare accepted.
- AC-BOOK-04: Invalid passenger data causes the entire booking operation to fail.
- AC-BOOK-05: Insufficient inventory causes no booking, passenger record, or inventory change.
- AC-BOOK-06: Concurrent attempts for the last seats produce at most one successful booking and no negative inventory.
- AC-BOOK-07: A failure during persistence rolls back all booking-related changes.

### 4.5 Module 4 — Booking Management

| ID | Requirement |
|---|---|
| FR-MGMT-001 | The system shall allow an authenticated CUSTOMER to retrieve that customer's booking history. |
| FR-MGMT-002 | The system shall allow an authenticated CUSTOMER to retrieve a booking by PNR only when that booking belongs to the customer or another server-authorized access rule permits it. |
| FR-MGMT-003 | The system shall reject unauthorized PNR lookup and shall not disclose whether an unknown PNR belongs to another customer. |
| FR-MGMT-004 | Booking details shall include PNR, flight, passenger details, cabin class, passenger count, fare snapshot, status, and timestamps. |
| FR-MGMT-005 | The system shall allow cancellation only for a booking that is owned by the authenticated customer and is eligible under the two-hour cutoff. |
| FR-MGMT-006 | The system shall reject cancellation of an already cancelled booking. |
| FR-MGMT-007 | The system shall reject cancellation requests made less than or equal to two hours before scheduled departure. |
| FR-MGMT-008 | The system shall restore exactly the booking passenger count to the original flight cabin inventory upon successful cancellation. |
| FR-MGMT-009 | The system shall change an eligible booking status to CANCELLED transactionally with inventory restoration. |
| FR-MGMT-010 | Repeated cancellation attempts shall not restore inventory more than once. |
| FR-MGMT-011 | Cancelled bookings shall remain retrievable through authorized PNR lookup with status CANCELLED. |
| FR-MGMT-012 | Cancellation and inventory restoration shall roll back together if persistence fails. |

#### Acceptance Criteria — Booking Management

- AC-MGMT-01: A customer can view only their own booking history.
- AC-MGMT-02: An authorized PNR lookup shows complete booking details.
- AC-MGMT-03: A booking owned by another customer cannot be accessed through history or PNR lookup.
- AC-MGMT-04: Cancellation more than two hours before departure restores inventory once and marks the booking CANCELLED.
- AC-MGMT-05: Cancellation at or after the cutoff is rejected and leaves status and inventory unchanged.
- AC-MGMT-06: Repeated cancellation returns an already-cancelled response and changes inventory zero additional times.
- AC-MGMT-07: A cancelled booking remains accessible to its owner with CANCELLED status.

### 4.6 Module 5 — Admin Management

| ID | Requirement |
|---|---|
| FR-ADMIN-001 | The system shall allow an authenticated ADMIN to create a unique airport record. |
| FR-ADMIN-002 | The system shall allow an authenticated ADMIN to update airport information without permitting duplicate airport codes. |
| FR-ADMIN-003 | The system shall allow an authenticated ADMIN to create a flight schedule. |
| FR-ADMIN-004 | The system shall allow an authenticated ADMIN to update flight schedule and flight information. |
| FR-ADMIN-005 | The system shall allow an authenticated ADMIN to create or update fares by cabin class for a flight. |
| FR-ADMIN-006 | The system shall allow an authenticated ADMIN to update cabin-class seat inventory for a flight. |
| FR-ADMIN-007 | Inventory updates shall never reduce available seats below zero or set available seats above total seats. |
| FR-ADMIN-008 | The system shall allow an authenticated ADMIN to view customer bookings. |
| FR-ADMIN-009 | Administrative endpoints shall reject unauthenticated users and non-ADMIN users. |
| FR-ADMIN-010 | The system shall not expose operational deletion endpoints for airports or flights in the MVP. |
| FR-ADMIN-011 | The system shall validate that a flight origin and destination are distinct, existing airports. |

#### Acceptance Criteria — Admin Management

- AC-ADMIN-01: An ADMIN can create and update airport and flight records; CUSTOMER users cannot.
- AC-ADMIN-02: Duplicate airport codes and duplicate flight fare/inventory identifiers are rejected.
- AC-ADMIN-03: Fare and inventory updates remain within the database constraints.
- AC-ADMIN-04: ADMIN booking review includes customer, PNR, flight, status, and required booking details.
- AC-ADMIN-05: No operational delete endpoint or UI action exists for airports or flights.

## 5. AeroMate AI Requirements

### 5.1 RAG Requirements

| ID | Requirement |
|---|---|
| FR-AI-001 | AeroMate shall answer supported airline-policy questions using approved Markdown knowledge-base documents. |
| FR-AI-002 | The knowledge ingestion process shall load, transform, split, embed, and index approved documents. |
| FR-AI-003 | Relevant document chunks shall be retrieved and supplied to the model as grounded context. |
| FR-AI-004 | AeroMate shall not state an airline policy when the knowledge base does not provide sufficient support. |
| FR-AI-005 | AeroMate shall clearly indicate when available documents do not contain a sufficient answer. |
| FR-AI-006 | PostgreSQL with pgvector is the preferred vector store, subject to compatibility. |
| FR-AI-007 | An unavailable or incompatible pgvector integration shall not block core booking functionality. |

### 5.2 Tool Calling Requirements

| ID | Requirement |
|---|---|
| FR-AI-008 | AeroMate shall expose controlled read-only tools for flight search and flight details. |
| FR-AI-009 | AeroMate shall expose controlled read-only tools for the authenticated customer's bookings and PNR lookup. |
| FR-AI-010 | Tool implementations shall reuse existing application services and authorization rules. |
| FR-AI-011 | User identity for `getMyBookings` and PNR ownership checks shall come only from the authenticated security context. |
| FR-AI-012 | The language model shall not supply or override user identity. |
| FR-AI-013 | AeroMate tools shall not perform booking creation, cancellation, payment, or administrative mutations. |
| FR-AI-014 | AeroMate shall require explicit normal authenticated application confirmation before any booking-related mutation initiated outside the read-only chat tools. |

### 5.3 JDBC Chat Memory Requirements

| ID | Requirement |
|---|---|
| FR-AI-015 | AeroMate shall use Spring AI JDBC Chat Memory with PostgreSQL for persistent conversation context. |
| FR-AI-016 | Each conversation shall be associated with a server-maintained conversation identifier and authenticated user. |
| FR-AI-017 | Conversation memory shall remain available after page refresh or page navigation. |
| FR-AI-018 | Conversation ownership metadata shall be separate from framework-managed message storage. |
| FR-AI-019 | The system shall reject access to another user's conversation identifier. |
| FR-AI-020 | A new conversation shall receive a new conversation identifier. |
| FR-AI-021 | Conversation history listing is optional and shall not be confused with mandatory conversation continuity. |

### 5.4 MCP Requirements

| ID | Requirement |
|---|---|
| FR-AI-022 | An MCP server shall expose selected read-only flight search and flight-detail capabilities. |
| FR-AI-023 | An MCP client shall discover and invoke the exposed tools through a supported Spring AI-compatible transport. |
| FR-AI-024 | MCP tools shall reuse existing application services. |
| FR-AI-025 | The MCP demonstration shall not expose booking creation, cancellation, inventory changes, fare changes, or administration. |

#### Acceptance Criteria — AeroMate

- AC-AI-01: A policy question with relevant indexed content receives a grounded answer or appropriate citation/context behavior supported by the implementation.
- AC-AI-02: A question outside the knowledge base produces an explicit insufficient-information response, not invented policy.
- AC-AI-03: `searchFlights` and `getFlightDetails` return application data and respect validation rules.
- AC-AI-04: `getMyBookings` cannot return another user's bookings.
- AC-AI-05: `getBookingDetails` cannot disclose a booking belonging to another customer.
- AC-AI-06: No AI tool creates or cancels a booking or changes inventory or fares.
- AC-AI-07: A conversation continues after refresh for the conversation's authenticated owner.
- AC-AI-08: Another authenticated or unauthenticated user cannot read or append to a conversation they do not own.
- AC-AI-09: The MCP client can discover and invoke the read-only flight MCP tools.

## 6. Non-Functional Requirements

### 6.1 Security

| ID | Requirement |
|---|---|
| NFR-SEC-001 | Protected APIs shall require valid JWT authentication. |
| NFR-SEC-002 | Customer and administrative authorization shall be enforced server-side. |
| NFR-SEC-003 | Passwords shall be stored using a secure industry-standard one-way password hashing mechanism. |
| NFR-SEC-004 | Secrets, API keys, passwords, and internal AI configuration shall not be committed to source control or returned in API responses. |
| NFR-SEC-005 | API responses shall use centralized exception handling and shall not expose stack traces or internal secrets. |
| NFR-SEC-006 | Input shall be validated for type, required fields, range, format, and ownership before business processing. |
| NFR-SEC-007 | The system shall prevent direct object access between customer accounts. |

### 6.2 Reliability and Availability

| ID | Requirement |
|---|---|
| NFR-REL-001 | Booking creation and cancellation shall execute with ACID database transactions. |
| NFR-REL-002 | Failed booking or cancellation operations shall leave no partial booking or inventory state. |
| NFR-REL-003 | AI service failure shall not make the core flight search or booking workflow unavailable unless the AI gateway is explicitly used in that workflow. |
| NFR-REL-004 | Database constraint violations shall be handled by centralized validation or exception handling rather than propagated as raw errors. |

### 6.3 Performance

| ID | Requirement |
|---|---|
| NFR-PERF-001 | Under the expected MVP demonstration load, normal flight search shall complete with a practical interactive response time; the exact threshold is an open question. |
| NFR-PERF-002 | Booking creation and cancellation shall complete or fail within a predictable transaction timeout. |
| NFR-PERF-003 | Database queries shall avoid known N+1 patterns for booking history and passenger detail retrieval. |
| NFR-PERF-004 | AI responses may exceed normal REST response time expectations, but loading/error states shall be provided by the frontend. |

### 6.4 Maintainability and Architecture

| ID | Requirement |
|---|---|
| NFR-MAIN-001 | The backend shall use a modular monolith with separated controller, service, repository, entity, and DTO responsibilities. |
| NFR-MAIN-002 | REST controllers shall handle HTTP concerns and delegate business decisions to services. |
| NFR-MAIN-003 | JPA entities shall not be returned directly through REST APIs. |
| NFR-MAIN-004 | AI components shall remain separate from core booking business logic. |
| NFR-MAIN-005 | API documentation shall describe endpoints, request validation, and relevant response codes. |
| NFR-MAIN-006 | Configuration values shall be externalized sufficiently to avoid committing environment-specific secrets. |

### 6.5 Data Consistency

| ID | Requirement |
|---|---|
| NFR-DATA-001 | Monetary values shall use `BigDecimal` and suitable decimal precision. |
| NFR-DATA-002 | Available seats shall never be negative or exceed total seats. |
| NFR-DATA-003 | PNR values shall be unique. |
| NFR-DATA-004 | Emails and airport codes shall be unique. |
| NFR-DATA-005 | Flight fare and inventory shall have unique flight-and-cabin combinations. |
| NFR-DATA-006 | A flight origin and destination shall be distinct. |
| NFR-DATA-007 | Cancellation shall restore inventory exactly once for each successful first cancellation. |

### 6.6 Usability and Accessibility

| ID | Requirement |
|---|---|
| NFR-USA-001 | The React interface shall provide responsive layouts for normal desktop and mobile viewport sizes. |
| NFR-USA-002 | Forms shall provide clear field validation feedback. |
| NFR-USA-003 | Search, booking, cancellation, and AI operations shall expose loading and error states. |
| NFR-USA-004 | Results and error messages shall be understandable without requiring database or stack-trace knowledge. |

### 6.7 Testability and Documentation

| ID | Requirement |
|---|---|
| NFR-TEST-001 | Critical booking, cancellation, authorization, search, and AI ownership scenarios shall be executable as repeatable tests or documented test procedures. |
| NFR-TEST-002 | APIs shall be discoverable and documentable through Swagger/OpenAPI. |
| NFR-TEST-003 | Each major implementation change shall be verifiable through automated tests or an actual repeatable manual test record. |

## 7. Business Rules and Validation Rules

### 7.1 Search and Flight Rules

- BR-FLIGHT-01: Only direct, one-way flights are supported.
- BR-FLIGHT-02: Origin and destination airports must be different.
- BR-FLIGHT-03: Passenger count must be positive.
- BR-FLIGHT-04: A flight is eligible only if the selected cabin has sufficient available seats.
- BR-FLIGHT-05: Direct flights shall have zero stops.
- BR-FLIGHT-06: Search fare and availability shall be validated again before booking.

### 7.2 Booking Rules

- BR-BOOK-01: The passenger count shall equal the number of passenger records.
- BR-BOOK-02: Passenger full name and date of birth are mandatory.
- BR-BOOK-03: Fare shall be calculated from backend data, not frontend input.
- BR-BOOK-04: Booking and seat reservation shall occur in one transaction.
- BR-BOOK-05: Concurrent bookings shall be protected from overselling.
- BR-BOOK-06: PNRs shall be globally unique within the system.

### 7.3 Cancellation Rules

- BR-CANCEL-01: Cancellation is permitted more than two hours before scheduled departure.
- BR-CANCEL-02: Cancellation at or after the two-hour cutoff is rejected.
- BR-CANCEL-03: Already cancelled bookings cannot be cancelled again.
- BR-CANCEL-04: Inventory is restored only once for a booking.
- BR-CANCEL-05: Cancelled bookings remain visible to authorized owners.

### 7.4 Administration Rules

- BR-ADMIN-01: Airport codes and airport identities shall remain unique.
- BR-ADMIN-02: Flight fares and inventory are maintained by cabin class.
- BR-ADMIN-03: Airports and flights shall not be operably deleted in the MVP.
- BR-ADMIN-04: Only users with ADMIN role may execute administrative operations.

### 7.5 AI and Safety Rules

- BR-AI-01: AeroMate shall not confirm, cancel, or create bookings.
- BR-AI-02: AeroMate shall not process payments or modify inventory.
- BR-AI-03: Tool identity data shall come from server authentication, not model output.
- BR-AI-04: Retrieved documents and model output are untrusted for executable instructions or authority overrides.
- BR-AI-05: Booking mutations require normal application workflows and explicit user confirmation.
- BR-AI-06: Unsupported policy questions shall receive an insufficient-information acknowledgment rather than a fabricated answer.

## 8. Important Business Scenarios

| ID | Scenario | Expected result |
|---|---|---|
| BS-001 | Search with invalid criteria | No flight list is returned; field-specific validation identifies invalid origin, destination, date, passenger count, or cabin class. |
| BS-002 | Search with insufficient seats | Matching flight is omitted because its selected cabin inventory is below passenger count. |
| BS-003 | Successful booking | Backend verifies data and inventory, creates passengers and booking, reserves seats, generates unique PNR, and returns success. |
| BS-004 | Concurrent requests for last available seats | Only one request can reserve the constrained seats successfully; remaining requests fail because of insufficient inventory and no overselling occurs. |
| BS-005 | Cancellation more than two hours before departure | Booking becomes CANCELLED and booked seats are restored exactly once. |
| BS-006 | Cancellation at or before the cutoff | Cancellation is rejected; status and inventory remain unchanged. |
| BS-007 | Repeated cancellation | Already-cancelled status is detected; no additional inventory restoration occurs. |
| BS-008 | Unauthorized booking access | Another customer's PNR or booking history data is not disclosed. |
| BS-009 | AI question with relevant knowledge | AeroMate answers using retrieved approved context and does not assert unsupported details. |
| BS-010 | AI question without relevant knowledge | AeroMate states that the knowledge base does not contain sufficient information; it does not invent a policy. |

## 9. Security and Authorization Requirements

- JWT authentication shall be used.
- CUSTOMER and ADMIN roles shall be defined server-side.
- Customer bookings, PNR lookup, and conversations shall be ownership-protected.
- Administrative endpoints shall require ADMIN authority.
- AeroMate tools shall use the same security context and domain services as normal application operations.
- The system shall use centralized exception handling and consistent API response structures.
- API consumers shall receive no internal exception message or stack trace.
- Database constraints shall enforce uniqueness and seat bounds as final data-integrity safeguards.

## 10. External System Dependencies and Assumptions

| Dependency | Requirement | Assumption/Risk |
|---|---|---|
| PostgreSQL | System of record for application, memory metadata, and preferred vector storage | Availability and local environment setup are required |
| Spring Boot/Spring Web | REST application runtime | Compatible dependency versions shall be selected |
| Spring Data JPA/Hibernate | Database persistence | Schema and constraints are not to be redefined in this SRS |
| Spring Security/JWT | Authentication and authorization | Token settings and algorithm details remain open where not specified |
| React/TypeScript/Vite/Tailwind | Customer and administrative UI | Browser-based environment is available |
| Capgemini AI Gateway | Gemini model access | Credentials and endpoint availability are environment dependencies |
| Spring AI | Chat, memory, RAG, tools, and MCP support | Dependency compatibility may vary with Spring Boot version |
| pgvector | Preferred vector storage | May be unavailable; a compatible fallback may be used |
| Markdown knowledge documents | RAG source | Approved policy content must be supplied for meaningful RAG testing |
| Supported MCP transport | MCP client/server interoperability | Exact transport is an implementation decision within project compatibility constraints |

## 11. MVP Limitations

The following are not included in the SkyBook MVP:

- Connecting or multi-stop flights
- Advanced seat maps or seat selection
- Real payment gateway processing
- Real airline or GDS integration
- Complex refunds
- Partial passenger cancellation
- Booking rescheduling
- Operational deletion of airports or flights
- Advanced analytics or reporting
- Microservices architecture
- Customer conversation listing if it is not needed for mandatory memory continuity
- Payment simulation unless all mandatory work is complete ahead of schedule

## 12. Future Enhancements

Future enhancement proposals shall not be treated as current requirements. Potential areas include:

- Payment-gateway demonstration
- Booking rescheduling
- Refund processing
- Seat maps and seat-level selection
- Connecting and multi-city search
- Rich conversation history administration
- Enhanced analytics and reporting
- Expanded airline policy source management
- Live airline/GDS integration
- Additional AI tools for approved business workflows

## 13. Open Questions

The following items are ambiguous in the proposal and require stakeholder review before implementation decisions are finalized:

1. What PNR format shall be used (length, character set, and uniqueness generation strategy)?
2. What is the exact password policy for registration and login?
3. What JWT expiry, refresh-token, and logout behavior are required?
4. What fare precision and rounding method shall be used for total booking fare?
5. What cabin-class values are available in the MVP, and are the values standardized by code or name?
6. What passenger-name and date-of-birth validation limits should apply beyond mandatory presence and valid date?
7. What is the minimum allowed future travel date relative to current system time?
8. How should exact two-hour cancellation cutoff comparisons handle timezone and departure-local-time semantics?
9. What specific booking fields are included in each administrative booking view?
10. What HTTP response conventions are expected for insufficient inventory and concurrent booking conflicts?
11. What is the minimum response-time objective for flight search and booking operations?
12. If pgvector is unavailable, which compatible vector-store or fallback behavior is approved?
13. What supported MCP transport shall be used given the selected Spring Boot and Spring AI versions?
14. Should conversation IDs be supplied by the frontend or generated by the backend, and what format is acceptable?
15. Is there a required maximum conversation history size or message retention policy?
16. What exact Markdown policy documents will be approved and seeded for RAG demonstration?
17. Is retrieval citation or source attribution mandatory in AeroMate responses, or is grounded answer behavior sufficient?

## 14. Requirements Traceability Summary

| Area | Identifier range | Purpose |
|---|---|---|
| Authentication | FR-AUTH-001 to FR-AUTH-010 | Identity and authorization |
| Flight search | FR-FLIGHT-001 to FR-FLIGHT-010 | Module 1 |
| Flight selection | FR-SELECT-001 to FR-SELECT-006 | Module 2 |
| Passenger and booking | FR-BOOKING-001 to FR-BOOKING-015 | Module 3 |
| Booking management | FR-MGMT-001 to FR-MGMT-012 | Module 4 |
| Administration | FR-ADMIN-001 to FR-ADMIN-011 | Module 5 |
| AeroMate | FR-AI-001 to FR-AI-025 | RAG, tools, memory, MCP |
| Non-functional | NFR-SEC, REL, PERF, MAIN, DATA, USA, TEST | Quality and operational requirements |

## 15. Acceptance and Readiness Notes

A major module is ready for acceptance when:

1. All requirement identifiers applicable to the module are implemented or explicitly marked out of scope.
2. Associated acceptance criteria pass through repeatable test evidence.
3. Authorization and ownership behavior are verified where applicable.
4. Centralized validation and error handling are active.
5. API and frontend behavior match this SRS and the project proposal.
6. Open questions affecting a requirement are resolved or explicitly retained as documented deviations.

This SRS shall remain **Draft — pending review** until stakeholders review it and approve or revise the open questions and requirement baseline.
