# 03 — SkyBook Backend REST API Implementation

## 1. Objective

Implement the complete REST API backend for SkyBook — AI-Assisted Airline Ticket Booking Platform.

This milestone focuses exclusively on conventional REST API development using Java, Spring Boot, Spring Data JPA, Hibernate, PostgreSQL and Spring Security.

The backend must support all five functional modules:

1. Flight Search
2. Flight Selection
3. Passenger and Booking Management
4. Booking Management
5. Admin Management

Authentication, authorization, validation, DTO mapping, exception handling and Swagger/OpenAPI are cross-cutting requirements.

**AI functionality is explicitly excluded from this milestone.** RAG, Tool Calling, Chat Memory, MCP and AeroMate chat endpoints will be implemented separately after the REST backend is complete.

### Approved policy decisions for this milestone

- Cabin classes for MVP are restricted to `ECONOMY` and `BUSINESS` only.
- JWT access-token expiration is fixed at 60 minutes (1 hour).
- Refresh-token functionality is out of scope for this MVP.
- Registration password policy requires a minimum of 8 characters, including at least one uppercase letter, one lowercase letter, one digit and one special character.
- Passwords must be hashed with BCrypt before persistence and must never be stored in plaintext.

---

## 2. Mandatory documentation review

Before implementation, inspect:

- `AGENTS.md`
- `docs/01-project-proposal/PROJECT_PROPOSAL.md`
- `docs/02-srs/SRS.md`
- `docs/03-uml-diagrams/use-case/SKYBOOK_USE_CASE.md`
- `docs/03-uml-diagrams/class/SKYBOOK_CLASS_DIAGRAM.md`
- `docs/04-er-diagram/SKYBOOK_ER_DIAGRAM.md`
- `docs/05-database-documentation/DATABASE_DESIGN.md`
- Existing backend source code and configuration

Use the actual paths in the repository if filenames or folder locations differ.

Treat approved project documents as the source of truth.

Before coding, prepare a concise implementation checklist containing:

- Approved entities and relationships
- Existing package structure
- API requirements
- Authentication and authorization requirements
- Business rules
- Any unresolved conflicts or missing decisions

Do not silently invent or change domain requirements. If a critical conflict exists between approved documents, stop and ask for clarification.

---

## 3. Technology and implementation constraints

Use the existing project configuration wherever possible.

- Java and the existing Spring Boot version
- Maven Wrapper
- Spring Web
- Spring Data JPA and Hibernate
- PostgreSQL
- Spring Security
- JWT-based stateless authentication
- Bean Validation
- Springdoc OpenAPI / Swagger UI, using a version compatible with the existing Spring Boot version
- JUnit 5 and Spring Boot Test

Do not unnecessarily upgrade existing dependencies.

Do not introduce Flyway, Liquibase, microservices, unnecessary design patterns or additional infrastructure.

Use the existing `application.properties` configuration.

Keep database credentials in environment variables. Never hardcode or expose passwords, JWT secrets or other credentials.

Hibernate schema generation must follow the existing approved local development configuration.

---

## 4. Architecture and code organization

Follow a clean layered architecture:

- Controller: HTTP endpoints and request/response handling
- Service: business logic and transaction boundaries
- Repository: database operations
- Entity: JPA persistence model
- DTO: API request and response contracts
- Mapper: entity-to-DTO and DTO-to-entity conversion where appropriate
- Security: authentication, JWT and authorization
- Exception: centralized exception handling
- Config: application configuration and OpenAPI setup

Use the existing package structure where it is suitable. Keep domain responsibilities separated.

Do not expose JPA entities directly from REST controllers.

Use constructor injection. Prefer clear, readable implementations over unnecessary abstraction.

Use appropriate DTOs for registration, login, flight search, flight details, booking requests, passenger details, booking responses, admin requests and error responses.

Ensure entity-to-DTO mapping is explicit and avoids accidental exposure of password hashes, internal security information or unnecessary relationships.

---

## 5. Authentication and authorization

Implement secure authentication before protected business operations.

### Required functionality

- Customer registration
- Customer login
- JWT access-token generation
- Password hashing using BCrypt
- JWT request authentication
- Stateless session management
- Role-based authorization
- Authenticated-user extraction from the Spring Security principal

### Suggested endpoints

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |

Use the role names and registration rules established in the approved documents.

Never allow a public registration request to assign the ADMIN role.

### Security requirements

- Validate registration and login inputs.
- Reject duplicate email addresses.
- Never store plain-text passwords.
- Do not return password hashes in API responses.
- Validate JWT signature, expiration and claims.
- Protect all non-public endpoints.
- Apply role restrictions to administrative endpoints.
- Ensure users cannot access another user's bookings by manipulating IDs or PNR values.
- Derive the authenticated user's identity from the security context, not from a client-supplied user ID.
- Keep JWT secrets outside source control.

Provide a clean authentication response DTO.

---

## 6. Module 1 — Flight Search

Implement flight search using PostgreSQL-backed flight data.

### Search criteria

- Origin airport
- Destination airport
- Travel date
- Passenger count
- Cabin class

Support direct flights only.

### Suggested endpoint

`GET /api/v1/flights/search`

Use suitable query parameters and validation.

### Requirements

- Validate airport codes and travel date.
- Origin and destination must differ.
- Return only eligible scheduled flights.
- Check the requested cabin class and available inventory.
- Ensure available seats satisfy the passenger count.
- Return useful flight information, including:
  - Flight ID and flight number
  - Airline name/code
  - Origin and destination
  - Departure and arrival times
  - Flight duration
  - Stops
  - Cabin class
  - Fare
  - Available seats
- Use appropriate date and time types.
- Use `BigDecimal` for monetary values.

Do not implement connecting flights, advanced search filters or external airline API integration.

---

## 7. Module 2 — Flight Selection and Details

Implement the backend functionality needed for a customer to inspect and select a flight.

### Suggested endpoint

`GET /api/v1/flights/{flightId}`

### Requirements

- Return detailed flight information.
- Include cabin-specific fare and inventory information.
- Handle unavailable or nonexistent flights correctly.
- Do not reserve seats merely because a user views flight details.
- Revalidate fare and availability during booking creation.

Do not trust fare values or available-seat counts submitted by the frontend.

---

## 8. Module 3 — Passenger and Booking Management

Implement the core booking workflow.

### Suggested endpoint

`POST /api/v1/bookings`

### Booking request

Use a request DTO containing:

- Selected flight ID
- Cabin class
- Passenger details

Use the passenger fields defined in the approved SRS and database design.

Do not accept a trusted total fare, user ID, booking status or PNR from the client.

### Booking service requirements

1. Obtain the authenticated customer from the security context.
2. Validate the flight and cabin class.
3. Validate passenger details.
4. Determine the passenger count from the passenger list.
5. Retrieve the current fare from the database.
6. Verify seat availability.
7. Calculate the total amount on the backend.
8. Generate a unique PNR.
9. Create the booking and passenger records.
10. Decrease available inventory.
11. Commit all booking and inventory changes in a single transaction.

### Transactional correctness

This is a critical business requirement.

Prevent overselling when multiple users attempt to book the last available seats simultaneously.

Use an appropriate database concurrency strategy, such as pessimistic locking or a conditional atomic inventory update.

Ensure that:

- Insufficient inventory rejects the booking.
- Failed booking creation does not leave inventory incorrectly reduced.
- Passenger records and booking records are persisted consistently.
- The booking and inventory changes succeed or fail together.
- PNR uniqueness is enforced by the database and handled safely by the application.

Use `BigDecimal` for fare calculations.

Do not implement real payment gateway integration.

---

## 9. Module 4 — Booking Management

Implement booking retrieval and cancellation.

### Suggested endpoints

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/v1/bookings/my` | Authenticated customer |
| GET | `/api/v1/bookings/{pnr}` | Booking owner or ADMIN |
| PATCH | `/api/v1/bookings/{pnr}/cancel` | Booking owner or ADMIN |

### Requirements

- Retrieve the authenticated customer's booking history.
- Retrieve booking details using PNR.
- Include passenger details, flight details, fare and booking status.
- Enforce booking ownership.
- Allow ADMIN to access bookings according to the approved authorization rules.
- Prevent unauthorized users from retrieving another customer's booking.

### Cancellation rules

- Only eligible confirmed bookings can be cancelled.
- Update booking status to CANCELLED.
- Restore the appropriate cabin inventory.
- Execute cancellation and inventory restoration transactionally.
- Repeated cancellation must not restore seats more than once.
- Return a meaningful response when cancellation is not allowed.

Do not implement complex refunds, partial cancellation or rescheduling.

Follow the cancellation policy defined in the approved documents. If a cutoff rule is not finalized, ask before implementing it.

---

## 10. Module 5 — Admin Management

Implement ADMIN-only management endpoints.

### Airport management

- Create airport
- Update airport
- Retrieve airport details
- List airports

Suggested endpoints:

- `POST /api/v1/admin/airports`
- `PUT /api/v1/admin/airports/{id}`
- `GET /api/v1/admin/airports`
- `GET /api/v1/admin/airports/{id}`

### Flight management

- Create scheduled flight
- Update scheduled flight
- Retrieve flight details
- List and manage scheduled flights

Suggested endpoints:

- `POST /api/v1/admin/flights`
- `PUT /api/v1/admin/flights/{id}`
- `GET /api/v1/admin/flights`
- `GET /api/v1/admin/flights/{id}`

### Fare and inventory management

- Configure cabin-specific fares.
- Configure initial cabin inventory.
- Update fares and inventory using validated admin operations.
- Prevent invalid negative values.
- Do not accidentally overwrite seats already reserved by customers.

### Booking administration

- List bookings for administrative review.
- Retrieve booking details.
- Apply only the management operations permitted by the approved requirements.

Every admin endpoint must enforce ADMIN authorization on the backend. Hiding frontend controls is not sufficient security.

---

## 11. API response and exception handling

Use consistent response structures.

Implement centralized exception handling using `@RestControllerAdvice`.

Handle common cases:

- Validation errors
- Invalid credentials
- Unauthorized access
- Forbidden access
- Resource not found
- Duplicate email or airport code
- Duplicate PNR
- Insufficient inventory
- Invalid booking or cancellation state
- Database constraint violations
- Unexpected server errors

Use appropriate HTTP status codes, such as:

- 200 OK
- 201 Created
- 204 No Content where appropriate
- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 500 Internal Server Error

Do not expose internal stack traces, database credentials, SQL details or sensitive implementation information in API error responses.

Keep response DTOs consistent across modules.

---

## 12. Swagger and OpenAPI documentation

Configure Springdoc OpenAPI and Swagger UI.

Requirements:

- Provide a meaningful API title and description.
- Organize endpoints by module using tags.
- Document request DTOs and response DTOs.
- Document validation constraints and common error responses.
- Configure a JWT Bearer authentication security scheme.
- Allow users to authorize with a JWT through Swagger UI.
- Ensure protected endpoints can be tested with the supplied token.

Verify that the Swagger UI and OpenAPI JSON endpoints load successfully.

Do not claim an endpoint is documented or working without testing it.

---

## 13. Testing requirements

Write meaningful tests rather than relying only on successful compilation.

### Authentication and security

- Successful registration
- Duplicate email registration
- Successful login
- Invalid password
- Invalid or expired JWT
- Customer cannot access admin endpoints
- ADMIN can access authorized admin endpoints

### Flight search

- Valid search
- Invalid airport codes
- Same origin and destination
- No matching flights
- Insufficient available seats
- Cabin-specific fare and inventory

### Booking

- Successful booking
- Invalid passenger details
- Nonexistent flight
- Insufficient inventory
- Backend fare calculation
- Unique PNR
- Inventory decrement
- Transaction rollback on failure
- Concurrent booking protection

### Booking management

- Customer booking history
- Valid PNR lookup
- Unauthorized booking access
- Successful cancellation
- Repeated cancellation
- Inventory restoration exactly once

### Admin

- Airport creation and update
- Flight creation and update
- Fare and inventory validation
- Customer denied admin access

Use appropriate unit and integration tests. Use the existing PostgreSQL configuration for database integration tests where feasible; do not silently change the application's database behavior to make tests pass.

---

## 14. Explicit exclusions

Do not implement any of the following in this milestone:

- AeroMate chat interface or chat endpoints
- Spring AI ChatClient
- RAG or vector-store integration
- Embedding models
- Tool Calling
- Spring AI Chat Memory
- MCP server or client
- React frontend
- Payment gateway
- External airline API integration
- Microservices
- Advanced analytics
- Connecting-flight engine

These belong to later milestones.

---

## 15. Implementation sequence

Implement the backend in the following order:

### Phase 1 — Foundation
- Review existing setup.
- Configure dependencies.
- Create common response and exception structures.
- Establish the package organization.

### Phase 2 — Authentication and Security
- User entity and repository adjustments if required.
- Registration and login DTOs.
- Password hashing.
- JWT generation and validation.
- Spring Security configuration.
- Role-based authorization.

### Phase 3 — Flight Search and Selection
- Flight-related entities and relationships.
- Repositories and search queries.
- Service layer.
- Flight search and details endpoints.

### Phase 4 — Booking Lifecycle
- Booking and passenger entities.
- DTO mapping.
- Transactional booking service.
- PNR generation.
- Inventory concurrency protection.
- Booking history, lookup and cancellation.

### Phase 5 — Admin Management
- Airport management.
- Flight management.
- Fare and inventory management.
- Admin booking operations.

### Phase 6 — Swagger and Verification
- OpenAPI configuration.
- JWT authorization in Swagger UI.
- Unit and integration tests.
- End-to-end booking lifecycle verification.

After each phase, run relevant tests and review the implementation before moving to the next phase. Do not leave broken code behind when moving between phases.

---

## 16. Final acceptance criteria

The REST backend is ready for review when:

1. The application starts successfully with PostgreSQL.
2. Database relationships are consistent with the approved ER diagram.
3. Customers can register and log in.
4. JWT authentication and role-based authorization work.
5. All five functional modules have their required REST endpoints.
6. Entity-to-DTO and DTO-to-entity mapping is correct.
7. No controller exposes JPA entities directly.
8. Booking and cancellation are transactionally correct.
9. Inventory cannot be oversold through concurrent bookings.
10. Booking ownership is enforced.
11. Admin operations are protected.
12. Swagger UI documents the REST API and supports JWT authorization.
13. Relevant automated tests pass.
14. No AI functionality has been implemented prematurely.

---

## 17. Completion report

At the end, provide:

- Final backend package structure.
- All entities and relationships implemented.
- All REST endpoints with HTTP methods and access roles.
- DTOs and mapping approach.
- Authentication and authorization implementation.
- Booking transaction and concurrency strategy.
- Swagger UI and OpenAPI URLs.
- Tests executed and their actual results.
- Known limitations and unresolved decisions.
- Files created, modified or deleted.

Never claim that tests, database operations or API calls succeeded unless they were actually executed and verified.

**Stop after completing this REST backend milestone. Do not begin AI development or React frontend development without my approval.**