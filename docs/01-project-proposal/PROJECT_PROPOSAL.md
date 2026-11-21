# SkyBook — AI-Assisted Airline Ticket Booking Platform

**Project Type:** Full-Stack Web Application with Generative AI  
**AI Assistant:** AeroMate  
**Architecture:** Modular Monolith  
**Development Timeline:** 4 Days

---

# 1. Introduction

SkyBook is a full-stack airline ticket booking platform designed to simplify flight discovery, ticket booking, and booking management.

The platform integrates an AI-powered virtual assistant named **AeroMate**, which helps users search for flights, retrieve booking information, and understand airline policies through natural language interactions.

The application will be developed using Java, Spring Boot, React, TypeScript, PostgreSQL, and Spring AI.

The project demonstrates practical software engineering concepts, including REST API development, database management, authentication, transactional operations, frontend integration, and modern generative AI capabilities.

# 2. Problem Statement

Traditional airline booking systems require users to navigate multiple interfaces to search for flights, compare available options, enter passenger details, and manage their bookings.

Users may also need to independently search for airline policies and booking-related information.

SkyBook aims to provide a centralized booking experience with an AI-powered assistant that makes flight discovery and access to relevant information more convenient.

The platform will also provide administrative functionality for managing airports, flight schedules, fares, and seat inventory.

# 3. Project Objectives

- Develop a functional full-stack airline ticket booking platform.
- Implement secure authentication and role-based authorization.
- Enable customers to search, select, book, retrieve, and cancel flights.
- Generate unique Passenger Name Records (PNRs).
- Implement administrative functionality for managing flight-related information.
- Integrate AeroMate using Spring AI.
- Demonstrate RAG, Tool Calling, persistent Chat Memory, and Model Context Protocol (MCP).
- Apply industry-standard software engineering practices, including layered architecture, validation, exception handling, and transactional data management.

# 4. Proposed Solution

SkyBook will provide two primary application interfaces.

## 4.1 Customer Interface

Customers will be able to:

- Register and log in securely.
- Search available flights using travel criteria.
- View flight schedules, fares, and seat availability.
- Select a suitable flight.
- Enter passenger information.
- Confirm bookings and receive a unique PNR.
- View booking history.
- Retrieve booking details using a PNR.
- Cancel eligible bookings.
- Interact with AeroMate for flight-related assistance.

## 4.2 Admin Interface

Administrators will be able to:

- Create and update airport information.
- Create and update flight schedules.
- Manage flight fares.
- Manage seat inventory.
- View customer bookings.

Administrative operations will be restricted to authenticated users with the ADMIN role.

## 4.3 AeroMate — AI Flight Assistant

AeroMate will provide conversational assistance using Spring AI.

It will:
- Answer airline policy questions using RAG.
- Retrieve flight and booking information using controlled application tools.
- Maintain persistent conversation context.
- Demonstrate MCP-based tool interoperability.

# 5. Functional Modules

## 5.1 Flight Search

Customers can search for available flights using:

- Origin airport.
- Destination airport.
- Travel date.
- Passenger count.
- Cabin class.

The application will validate search criteria and retrieve matching direct, one-way flights from PostgreSQL.

Only flights with sufficient inventory for the requested passenger count will be displayed.

## 5.2 Flight Selection

The application will display matching flight information:

- Airline name and flight number.
- Departure and arrival airports.
- Departure and arrival times.
- Flight duration.
- Number of stops (always zero for the MVP).
- Available seats.
- Fare for the selected cabin class.

Customers can select a flight and proceed to booking.

The backend will independently validate the selected flight, fare, and inventory before confirming a booking.

## 5.3 Passenger and Booking Management

Customers can:

- Enter passenger details.
- Review flight and booking information.
- View the calculated total fare.
- Confirm their booking.
- Receive a unique PNR.

### Passenger information

For the MVP, each passenger will have:

- Full name.
- Date of birth.

Passenger information will be stored through a `BookingPassenger` relationship associated with the booking.

### Booking process

1. Validate the authenticated customer.
2. Validate passenger information.
3. Verify flight and cabin-class inventory.
4. Calculate the fare on the backend.
5. Reserve the required seats.
6. Generate a unique PNR.
7. Persist the booking and passenger records.

The booking process will execute transactionally to prevent inconsistent records and duplicate seat reservations.

## 5.4 Booking Management

Customers can:

- View their booking history.
- Retrieve booking details using a PNR.
- View passenger and flight information.
- View booking status.
- Cancel eligible bookings.

### Cancellation rules

- A booking can be cancelled up to two hours before scheduled departure.
- Cancellation requests after the cutoff will be rejected.
- Already cancelled bookings cannot be cancelled again.
- Cancelled bookings remain accessible through PNR lookup with their CANCELLED status.
- Seat inventory will be restored only once.

Cancellation and inventory restoration will execute transactionally.

## 5.5 Admin Management

Administrators can:

- Create airports.
- Update airport information.
- Create flight schedules.
- Update flight information.
- Manage flight fares.
- Update cabin-class seat inventory.
- View customer bookings.

The MVP will not support operational deletion of airport or flight records to avoid breaking existing booking relationships.

# 6. AI Integration — AeroMate

AeroMate will demonstrate four major Spring AI capabilities.

## 6.1 Retrieval-Augmented Generation (RAG)

AeroMate will answer airline-related questions using a small knowledge base containing Markdown documents.

The knowledge base will include:

- Baggage allowances.
- Cancellation policies.
- Booking guidelines.
- Check-in procedures.
- Passenger guidelines.

### RAG workflow

1. Load airline policy documents.
2. Transform and split documents into smaller chunks.
3. Generate embeddings.
4. Store embeddings in a vector database.
5. Retrieve relevant information when a user asks a question.
6. Supply retrieved context to the language model.
7. Generate a contextually grounded response.

PostgreSQL with pgvector will be the preferred vector store, subject to environment and embedding-provider compatibility.

If pgvector or the selected embedding integration is unavailable, a compatible alternative will be used without delaying the core booking functionality.

AeroMate must acknowledge when the available knowledge base does not contain sufficient information instead of inventing policies.

## 6.2 Tool Calling

AeroMate will use controlled application tools to retrieve application data.

Planned read-only tools:

| Tool | Responsibility |
|---|---|
| `searchFlights` | Search available flights |
| `getFlightDetails` | Retrieve flight information |
| `getMyBookings` | Retrieve authenticated customer's bookings |
| `getBookingDetails` | Retrieve an accessible booking using its PNR |

These tools will reuse existing application services.

Security requirements:

- User identity must come from the authenticated security context.
- The language model must not supply or override user identity.
- Customers must not access other customers' bookings.
- AI tools must not bypass application authorization.
- Booking creation, cancellation, and administrative modifications will remain in normal authenticated application workflows.

## 6.3 Persistent Chat Memory

AeroMate will use Spring AI JDBC Chat Memory with PostgreSQL.

Capabilities:

- Maintain conversation context across multiple messages.
- Persist message history.
- Continue conversations after page refresh.
- Associate conversations with authenticated users.
- Prevent unauthorized access to other users' conversations.

Conversation ownership metadata will be maintained separately from Spring AI's framework-managed message storage.

Conversation listing is optional; persistent conversation continuity is mandatory.

## 6.4 Model Context Protocol (MCP)

The project will include a minimal MCP demonstration using Spring AI.

An MCP server will expose selected read-only flight capabilities:

- Flight search.
- Flight details.

An MCP client will discover and invoke these tools through a supported MCP transport.

The MCP demonstration will reuse existing application services and will not expose booking mutations.

This demonstrates MCP client-server interoperability independently of normal LLM Tool Calling.

# 7. Technology Stack

| Layer | Technology |
|---|---|
| Programming Language | Java |
| Backend Framework | Spring Boot |
| REST API | Spring Web |
| Persistence | Spring Data JPA, Hibernate |
| Security | Spring Security, JWT |
| Validation | Jakarta Bean Validation |
| Database | PostgreSQL |
| AI Framework | Spring AI |
| Language Model | Gemini through Capgemini AI Gateway |
| Vector Store | PostgreSQL with pgvector |
| Chat Memory | Spring AI JDBC Chat Memory |
| AI Interoperability | Spring AI MCP |
| Frontend | React, TypeScript |
| Styling | Tailwind CSS |
| Frontend Build Tool | Vite |
| Backend Build Tool | Maven |
| API Documentation | Swagger / OpenAPI |
| Development Tools | VS Code, Git, GitHub Copilot |

All Spring AI dependencies and MCP transport choices will use versions compatible with the selected Spring Boot version and the available Capgemini environment.

# 8. System Architecture

SkyBook will follow a modular monolith architecture.

The backend will be organized into functional modules within a single Spring Boot application.

## 8.1 Backend Structure

```text
com.skybook
│
├── config/
│
├── common/
│   ├── exception/
│   ├── response/
│   └── util/
│
├── auth/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   └── security/
│
├── airport/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   ├── entity/
│   └── repository/
│
├── flight/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   └── provider/
│
├── booking/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   ├── entity/
│   └── repository/
│
├── admin/
│   ├── controller/
│   └── service/
│
└── ai/
    ├── controller/
    ├── service/
    ├── rag/
    ├── tools/
    ├── memory/
    └── mcp/
```

## 8.2 Frontend Structure

```text
src/
│
├── app/
│   ├── router.tsx
│   └── providers.tsx
│
├── components/
│   ├── common/
│   ├── layout/
│   └── forms/
│
├── features/
│   ├── auth/
│   ├── flights/
│   ├── bookings/
│   ├── admin/
│   └── aeromate/
│
├── services/
│   └── api.ts
│
├── hooks/
├── types/
├── utils/
└── pages/
```

The React frontend will communicate with the Spring Boot backend through REST APIs.

# 9. Database Design

The following entities will form the core application database.

| Entity | Responsibility |
|---|---|
| User | Stores customer and administrator accounts |
| Airport | Stores airport codes, names, cities, and countries |
| Flight | Stores flight numbers, airline information, routes, schedules, and status |
| FlightInventory | Stores seat capacity and availability by cabin class |
| FlightFare | Stores flight fares by cabin class |
| Booking | Stores PNR, customer, flight, cabin class, passenger count, fare snapshot, status, and timestamps |
| BookingPassenger | Stores passenger details associated with a booking |

## 9.1 Entity Relationships

- One User can have multiple Bookings.
- One Airport can be the origin of multiple Flights.
- One Airport can be the destination of multiple Flights.
- One Flight can have multiple FlightInventory records.
- One Flight can have multiple FlightFare records.
- One User can have multiple Bookings.
- One Booking can contain multiple BookingPassenger records.
- One Booking belongs to one Flight.

Airline name and airline code will be stored directly in the Flight entity for the MVP, avoiding an unnecessary additional entity.

## 9.2 AI-related Persistence

- Spring AI JDBC Chat Memory tables will store conversational messages.
- Conversation metadata will associate conversation IDs with authenticated users.
- PostgreSQL vector storage will maintain embeddings for airline knowledge-base documents.

## 9.3 Important Database Constraints

- Unique email address for each user.
- Unique airport code.
- Unique PNR for each booking.
- Unique `(flight_id, cabin_class)` for inventory.
- Unique `(flight_id, cabin_class)` for fare.
- Available seats must never be negative.
- Available seats must never exceed total seats.
- Origin and destination airports must be different.

The booking record will retain the cabin class, passenger count, and fare snapshot required for booking history and inventory restoration.

# 10. Core REST API Design

All application APIs will use the `/api/v1` prefix.

## 10.1 Authentication APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/auth/register` | Register customer |
| POST | `/auth/login` | Authenticate user |

## 10.2 Flight APIs

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/flights/search` | Search available flights |
| GET | `/flights/{id}` | Retrieve flight details |

Search parameters will include origin, destination, travel date, passenger count, and cabin class.

## 10.3 Booking APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/bookings` | Create booking |
| GET | `/bookings/my` | Retrieve authenticated user's bookings |
| GET | `/bookings/{pnr}` | Retrieve booking details |
| PATCH | `/bookings/{pnr}/cancel` | Cancel eligible booking |

## 10.4 Admin APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/admin/airports` | Create airport |
| PUT | `/admin/airports/{id}` | Update airport |
| POST | `/admin/flights` | Create flight |
| PUT | `/admin/flights/{id}` | Update flight |
| PUT | `/admin/flights/{id}/fares` | Update flight fares |
| PUT | `/admin/flights/{id}/inventory` | Update seat inventory |
| GET | `/admin/bookings` | View customer bookings |

Operational deletion endpoints are excluded from the MVP.

## 10.5 AI APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/ai/chat` | Send message to AeroMate using a conversation ID |

The response will include the assistant's answer and conversation ID.

Conversation listing and history-management APIs are optional enhancements.

All APIs will use appropriate request and response DTOs. JPA entities will not be exposed directly.

# 11. Security and Business Rules

## Authentication and Authorization

- JWT-based authentication.
- Role-based authorization using CUSTOMER and ADMIN roles.
- Customers can only access their own bookings and conversations.
- Administrative endpoints require ADMIN authorization.
- AI tools must follow the same authentication and authorization rules as normal application services.

## Booking Rules

- Passenger information must be validated on the backend.
- Total booking fare must be calculated by the backend.
- PNR values must be unique.
- The requested inventory must be available before confirming a booking.
- Booking creation and seat reservation must execute within a database transaction.
- Database-level concurrency protection must prevent overselling.
- Failed booking operations must roll back all associated changes.

## Cancellation Rules

- Cancellation is allowed up to two hours before departure.
- Cancellation after the cutoff is rejected.
- Repeated cancellation must not restore inventory again.
- Booking status and seat restoration must be updated transactionally.

## General Engineering Rules

- Use DTOs for API communication.
- Apply Bean Validation.
- Implement centralized exception handling.
- Use BigDecimal for monetary calculations.
- Enforce database constraints.
- Document APIs using Swagger/OpenAPI.

# 12. Project Scope and Limitations

## 12.1 Included in MVP

- Direct, one-way flight search.
- Flight selection and fare display.
- Passenger information and booking confirmation.
- Unique PNR generation.
- Transactional seat reservation.
- Booking history and PNR lookup.
- Booking cancellation and seat restoration.
- Basic airport and flight administration.
- Flight schedule management.
- Fare and inventory management.
- JWT authentication and role-based authorization.
- AeroMate RAG.
- Spring AI Tool Calling.
- Persistent JDBC Chat Memory.
- Minimal MCP client-server demonstration.
- Simulated flight data stored in PostgreSQL.

## 12.2 Excluded from MVP

- Connecting and multi-stop flight search.
- Advanced seat maps and seat selection.
- Real payment gateway integration.
- Real airline or GDS API integration.
- Complex refund processing.
- Partial passenger cancellation.
- Booking rescheduling.
- Operational deletion of airports and flights.
- Advanced analytics and reporting.
- Microservices architecture.

Payment simulation may be considered only if all mandatory functionality is completed ahead of schedule.

# 13. Four-Day Implementation Plan

## Day 1 — Foundation and Flight Management

- Finalize SRS, architecture, and ER diagram.
- Initialize Spring Boot and React projects.
- Configure PostgreSQL and Maven dependencies.
- Establish basic Spring Security configuration.
- Implement core entities and repositories.
- Add simulated airport and flight data.
- Implement flight search and flight details APIs.
- Verify APIs using Postman or Swagger.

**Expected outcome:** Running application foundation with working flight search.

## Day 2 — Authentication and Booking Lifecycle

- Complete JWT authentication.
- Implement CUSTOMER and ADMIN authorization.
- Implement passenger management.
- Develop booking creation and PNR generation.
- Implement transactional seat reservation.
- Implement booking history and PNR lookup.
- Implement cancellation and seat restoration.
- Implement essential admin APIs.
- Test booking ownership and concurrent inventory protection.

**Expected outcome:** Complete and tested backend booking lifecycle.

## Day 3 — Frontend and AI Foundation

- Develop React customer interface.
- Implement flight search and selection screens.
- Develop passenger and booking forms.
- Implement booking history and cancellation screens.
- Develop basic admin interface.
- Integrate frontend with REST APIs.
- Configure Capgemini AI Gateway and Spring AI.
- Validate language model and embedding connectivity.
- Implement initial AeroMate RAG and Tool Calling.

**Expected outcome:** Working full-stack application with initial AI integration.

## Day 4 — AI Completion, Testing, and Documentation

- Complete RAG integration.
- Complete read-only Tool Calling.
- Implement persistent JDBC Chat Memory.
- Implement minimal MCP server and client demonstration.
- Test AI conversation ownership and tool authorization.
- Perform end-to-end booking tests.
- Verify cancellation and inventory consistency.
- Fix integration and validation issues.
- Complete Swagger documentation.
- Prepare project report and final presentation.

**Expected outcome:** Working full-stack MVP with demonstrated Spring AI capabilities and project documentation.

### Implementation Priority

1. Database and backend correctness.
2. Flight search and selection.
3. Booking lifecycle and security.
4. Frontend integration.
5. RAG and Tool Calling.
6. Persistent Chat Memory.
7. MCP demonstration.
8. UI polish and optional enhancements.

If an AI infrastructure dependency becomes unavailable, use a compatible fallback where possible and document any limitation. Do not sacrifice the core booking workflow to complete optional enhancements.

# 14. Project Deliverables

The project will maintain the following ten deliverables:

1. Project Proposal.
2. Software Requirements Specification (SRS).
3. System Architecture Diagram.
4. Entity Relationship Diagram (ERD).
5. Backend Source Code.
6. Frontend Source Code.
7. API Documentation (Swagger/OpenAPI).
8. AI Integration Documentation covering RAG, Tool Calling, Chat Memory, and MCP.
9. Test Cases and Actual Execution Results.
10. Final Project Report and Presentation.

# 15. Expected Outcome

The expected outcome is a functional full-stack airline booking application demonstrating practical software engineering, secure backend development, database design, frontend integration, and generative AI capabilities.

The platform will support the complete customer booking lifecycle:

**Search Flights → Select Flight → Enter Passenger Details → Confirm Booking → Generate PNR → Retrieve Booking → Cancel Booking → Restore Inventory**

AeroMate will complement the application by providing policy-based answers, controlled access to application information, persistent conversational context, and MCP-based tool interoperability.

SkyBook aims to demonstrate how traditional enterprise application development can be combined with modern generative AI capabilities using the Java and Spring ecosystem.