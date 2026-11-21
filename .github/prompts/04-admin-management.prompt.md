# SkyBook — Phase 5: Admin Management Implementation

## Objective

Implement the Admin Management module for SkyBook, extending the existing Spring Boot REST backend.

This phase must provide secure, role-restricted administrative APIs for managing airports, flights, fares, inventory, and viewing/managing bookings.

Follow the existing project architecture and implementation conventions. Reuse existing entities, repositories, DTOs, services, authentication, and exception handling wherever possible.

## 1. Before implementation

Review these files first:

- `AGENTS.md`
- `docs/PROJECT_PROPOSAL.md`
- `docs/SRS.md`
- `docs/SKYBOOK_ER_DIAGRAM.md`
- `docs/SKYBOOK_CLASS_DIAGRAM.md`
- `docs/DATABASE_DESIGN.md`
- Existing authentication, airport, flight, and booking packages.

Inspect the current implementation before making changes. Do not assume that a class, method, endpoint, or business rule is missing without checking.

If the existing code conflicts with the approved documentation, report the conflict and ask for clarification instead of silently changing the design.

## 2. Admin authorization

- All administrative endpoints must require the `ADMIN` role.
- Customers must receive HTTP `403 Forbidden` when accessing administrative endpoints.
- Unauthenticated requests must receive HTTP `401 Unauthorized`.
- Reuse the existing Spring Security and JWT implementation.
- Do not introduce a second authentication mechanism.
- Do not allow public registration with an ADMIN role.
- Do not hardcode admin credentials or expose a public admin-registration endpoint.
- Document a safe local-development method for promoting an existing registered account to ADMIN, using the existing role representation.

## 3. Airport Management

Implement the following endpoints:

- `POST /api/v1/admin/airports` — create an airport.
- `GET /api/v1/admin/airports` — list airports.
- `GET /api/v1/admin/airports/{id}` — get airport details.
- `PUT /api/v1/admin/airports/{id}` — update an airport.
- `DELETE /api/v1/admin/airports/{id}` — delete an airport only when it is safe to do so.

Requirements:

- Validate airport code, name, city, and country.
- Prevent duplicate airport codes.
- Return appropriate HTTP status codes.
- Do not allow deletion of airports referenced by existing flights.
- Return a meaningful conflict response when deletion is not permitted.

## 4. Flight and Schedule Management

Implement:

- `POST /api/v1/admin/flights` — create a flight schedule.
- `GET /api/v1/admin/flights` — list flights.
- `GET /api/v1/admin/flights/{id}` — retrieve flight details.
- `PUT /api/v1/admin/flights/{id}` — update a flight schedule.
- `DELETE /api/v1/admin/flights/{id}` — delete a flight only when safe.

Requirements:

- Reuse the existing `Flight` entity and airport relationships.
- Validate flight number, origin, destination, departure and arrival timestamps, and other existing required fields.
- Origin and destination must be different.
- Arrival must be after departure.
- Reference valid existing airports.
- Preserve the existing direct-flight-only MVP scope.
- Do not introduce connecting-flight logic.
- Do not allow destructive changes that would invalidate confirmed bookings or existing inventory.
- Use DTOs rather than exposing JPA entities.

## 5. Fare and Inventory Management

Implement suitable endpoints under the admin flight resource, following existing project conventions:

- Create or update fares for a flight and cabin class.
- Create or update inventory for a flight and cabin class.
- Retrieve the fare and inventory details for a flight.

Requirements:

- Support only the approved MVP cabin classes: `ECONOMY` and `BUSINESS`.
- Use `BigDecimal` for monetary values.
- Validate that fares are positive.
- Validate that total seats are non-negative and available seats are between zero and total seats.
- Prevent duplicate fare or inventory records for the same flight and cabin class.
- Do not accidentally reset or increase available seats in a way that could oversell flights.
- Protect existing bookings when inventory is changed.
- Reuse existing entities, repositories, and business logic.

## 6. Admin Booking Management

Implement:

- `GET /api/v1/admin/bookings` — list all bookings.
- `GET /api/v1/admin/bookings/{pnr}` — retrieve a booking by PNR.
- Support useful basic pagination if the existing project conventions allow it without unnecessary complexity.
- Allow an administrator to cancel a booking only through the existing booking cancellation business logic, if supported by the current design.

Requirements:

- Do not create a second cancellation implementation.
- Ensure cancellation restores inventory exactly once.
- Do not allow arbitrary booking status changes.
- Return DTOs, not entities.
- Preserve existing booking and passenger data.

## 7. Engineering Requirements

- Follow the existing package structure.
- Maintain controller → service → repository separation.
- Use request and response DTOs.
- Apply Bean Validation.
- Use centralized exception handling.
- Use appropriate HTTP status codes.
- Use transactions for operations that update related records.
- Prevent unauthorized access and invalid state transitions.
- Avoid duplicate business logic.
- Keep implementation consistent with existing naming and response conventions.
- Do not add unnecessary dependencies or introduce microservices.
- Do not modify working authentication or customer booking flows unless required for a verified integration issue.

## 8. Testing and Verification

Create or update tests for:

- Airport creation, retrieval, update, duplicate code rejection, and deletion restrictions.
- Flight creation, retrieval, update, invalid schedule rejection, and deletion restrictions.
- Fare validation and updates.
- Inventory validation and updates.
- Admin booking listing and PNR lookup.
- Admin cancellation, if implemented.
- Customer access to admin endpoints is rejected.
- Unauthenticated access to admin endpoints is rejected.
- ADMIN access is successful.

Run the Maven wrapper test suite:

`.\mvnw.cmd test`

Report the actual results:

- Number of tests executed.
- Failures and errors.
- Build status.
- Any tests not executed and why.

Do not claim that an endpoint works unless it has been tested. Do not claim concurrency safety based only on unit tests.

## 9. Strict Scope Restrictions

Do not implement:

- AI or AeroMate.
- RAG, Tool Calling, Chat Memory, or MCP.
- Payment gateway or payment simulation.
- Frontend or UI.
- External airline APIs.
- Advanced analytics.
- Connecting flights.
- New authentication mechanisms.

These belong to other milestones or future enhancements.

## 10. Completion Criteria

Phase 5 is complete only when:

1. Admin airport APIs work.
2. Admin flight and schedule APIs work.
3. Fare and inventory management APIs work.
4. Admin booking retrieval and permitted management operations work.
5. ADMIN authorization is verified.
6. Existing customer authentication, flight search, and booking functionality remain intact.
7. Tests and Maven build results are reported accurately.

After implementation and verification, stop and wait for my review. Do not begin AI development or frontend development automatically.