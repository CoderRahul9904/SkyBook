
# Phase 8 — SkyBook React Frontend Implementation

## Role

Act as a Senior React.js / TypeScript Frontend Engineer, UI/UX Designer, and Software Integration Engineer.

Your responsibility is to build a polished, responsive SkyBook frontend that integrates with the existing Spring Boot backend.

The existing backend is the source of truth. Never invent API endpoints, DTO fields, response structures, enum values, authentication rules, or business logic.

## 1. Mandatory Repository Inspection

Before writing frontend code, inspect the existing repository.

Read:

- `AGENTS.md`
- `docs/PHASE7_HANDOFF.md`
- Project proposal
- SRS documentation
- `docs/AI_INTEGRATION.md`
- `docs/TESTING.md`
- Existing backend controllers
- Request and response DTOs
- Services and repositories
- Spring Security and JWT configuration
- Global exception handling
- Swagger/OpenAPI documentation
- Existing frontend files, if any

First prepare an API contract inventory containing:

- HTTP method and exact endpoint.
- Authentication requirement.
- Required user role.
- Request parameters and body fields.
- Response structure.
- Important enum values.
- Success and error status codes.

Prioritize actual backend controllers, DTOs, and Swagger documentation over assumptions in older documentation.

Do not start implementing components until the API contract is understood.

## 2. Technology Stack

Use the following stack:

- React.js
- TypeScript
- Vite
- Tailwind CSS
- React Router
- Axios
- React Hook Form
- Zod
- Lucide React icons

Reuse existing dependencies and project configuration wherever possible.

Avoid unnecessary dependencies, Redux, and large UI frameworks unless there is a genuine requirement.

## 3. Functional Requirements

### A. Authentication

Implement:

- User registration.
- User login.
- Form validation.
- JWT authentication.
- Protected routes.
- Role-based navigation.
- Logout.
- Handling of expired or invalid authentication tokens.

Use the exact authentication mechanism implemented by the backend.

### B. Flight Search

Build a flight search interface containing:

- Origin airport.
- Destination airport.
- Departure date.
- Passenger count.
- Cabin class.

Implement search using the actual backend endpoint.

Display flight results using the fields actually returned by the backend, such as:

- Airline and flight number.
- Departure airport and time.
- Arrival airport and time.
- Flight duration.
- Stops.
- Fare.
- Available seats.

Include loading, empty, and error states.

### C. Flight Selection

Implement:

- Flight result cards.
- Flight details.
- Fare display.
- Flight selection.
- Continue to passenger details.

Do not calculate fares or availability independently in the frontend.

### D. Passenger and Booking

Implement:

- Passenger details form.
- Client-side validation using backend-compatible rules.
- Booking submission.
- Booking confirmation.
- PNR display.
- Booking details.

The backend remains responsible for:

- Fare calculation.
- Seat availability.
- Inventory updates.
- Booking validation.
- PNR generation.
- Transactional booking operations.

Never simulate successful bookings.

### E. Booking Management

Implement:

- My bookings.
- Booking history.
- PNR lookup.
- Booking details.
- Booking status.
- Cancellation.
- Cancellation confirmation dialog.

Display the actual booking status returned by the backend.

Handle already-cancelled bookings and other backend errors correctly.

### F. Admin Dashboard

Implement admin pages only for operations supported by existing backend endpoints.

Required areas:

- Airport management.
- Flight and schedule management.
- Fare management.
- Inventory management.
- Booking management.

Use role-based route protection.

Do not create unsupported DELETE operations or other mutations.

### G. AeroMate AI Assistant

Integrate the existing AeroMate AI REST API.

Implement:

- Chat interface.
- Start a conversation.
- Send messages.
- Display assistant responses.
- Continue existing conversations.
- Conversation history, if supported.
- Loading indicators.
- Error handling.
- Empty states.
- Retry behavior.

Use the actual conversation IDs returned by the backend.

Display citations or policy references only if the backend provides them.

The frontend must communicate with the backend AI REST API.

Do not implement RAG, Tool Calling, or Chat Memory in the browser. These responsibilities belong to the Spring Boot backend.

Do not create a direct browser-side MCP client.

## 4. API Integration Architecture

Create a centralized API client.

Requirements:

- Configure the backend base URL using `VITE_API_BASE_URL`.
- Create `.env.example`.
- Never commit real secrets or local `.env` files.
- Attach JWT tokens using the backend's actual authentication scheme.
- Centralize error handling.
- Handle HTTP 400, 401, 403, 404, and 500 responses.
- Handle network failures.
- Use typed TypeScript interfaces matching backend DTOs.
- Separate API functions from React components.
- Avoid unnecessary duplicate requests.

Do not hardcode fake flight, booking, user, or admin data.

If mock data is used during isolated UI development, keep it separate and ensure the actual application uses the backend.

Do not weaken backend security or change authentication behavior to make the frontend work.

## 5. UI/UX Requirements

Build a professional, modern airline-booking experience.

Design expectations:

- Clean and modern interface.
- Consistent typography.
- Professional travel-focused visual identity.
- Responsive desktop, tablet, and mobile layouts.
- Consistent spacing and colors.
- Reusable UI components.
- Accessible forms and labels.
- Keyboard navigation.
- Visible focus states.
- Loading skeletons.
- Empty states.
- Success and error messages.
- Confirmation dialogs.
- Consistent currency, date, and time formatting.

Avoid excessive animations and unnecessary visual clutter.

No dead buttons, placeholder links, or controls that do nothing.

## 6. Suggested Frontend Structure

Adapt this structure to the existing repository.

frontend/
  src/
    app/
      router.tsx
      providers.tsx

    components/
      common/
      layout/
      forms/

    features/
      auth/
      flights/
      bookings/
      admin/
      aeromate/

    services/
      api/
      auth/

    hooks/
    types/
    utils/
    pages/
    styles/

    main.tsx

Keep feature-specific components, services, types, and validation schemas organized together.

Avoid unnecessary abstraction.

## 7. Implementation Sequence

Complete the following tasks in order:

1. Inspect the repository and backend API contracts.
2. Identify missing or unsupported backend endpoints.
3. Set up or adapt React, TypeScript, Vite, and Tailwind CSS.
4. Create the application layout and reusable components.
5. Implement the centralized API client.
6. Implement authentication and protected routes.
7. Implement flight search and flight results.
8. Implement flight selection.
9. Implement passenger details and booking.
10. Implement booking history, PNR lookup, details, and cancellation.
11. Implement admin dashboard operations supported by the backend.
12. Integrate the AeroMate chat interface.
13. Implement loading, error, empty, and success states.
14. Verify responsive layouts.
15. Test frontend-backend integration.
16. Update README documentation.

Do not stop after creating a plan. Continue implementation and testing.

If a backend feature is unavailable, do not invent an endpoint or fake the functionality. Document the missing capability and continue with supported features.

## 8. Testing and Verification

Run the scripts actually defined in package.json.

Examples:

npm install
npm run lint
npm run type-check
npm run test
npm run build

Add appropriate scripts and tests if they are missing.

Verify:

- Registration.
- Login.
- JWT authentication.
- Protected routes.
- Flight search.
- Flight selection.
- Passenger validation.
- Booking submission.
- Booking confirmation and PNR.
- Booking history.
- Booking details.
- Cancellation.
- Admin authorization.
- Supported admin operations.
- AeroMate chat.
- Conversation continuity.
- Unauthorized and forbidden responses.
- Responsive layouts.

Run live backend integration tests whenever the backend is available.

Clearly distinguish mocked tests from actual backend integration tests.

Do not claim successful end-to-end verification without evidence.

## 9. Constraints

- Do not unnecessarily modify stable backend modules.
- Do not change database schema.
- Do not introduce microservices.
- Do not implement payment gateway integration.
- Do not implement external airline APIs.
- Do not introduce connecting-flight functionality unless already supported.
- Do not implement MCP directly in the frontend.
- Do not expose API keys or credentials.
- Do not commit local environment files.
- Do not delete existing documentation.
- Do not delete, rename, overwrite, or replace `docs/PHASE7_HANDOFF.md`.

Keep the implementation focused on Phase 8.

## 10. Final Report

At completion, report:

1. Pages and features implemented.
2. Backend endpoints integrated.
3. Important files created or modified.
4. Dependencies added.
5. Frontend folder structure.
6. Commands to run the application.
7. Lint and TypeScript results.
8. Test results.
9. Production build results.
10. Actual frontend-backend integration results.
11. Remaining backend gaps or blockers.

The final objective is a complete, polished, responsive React frontend that integrates correctly with the existing SkyBook Spring Boot backend.

The backend is the source of truth. Do not build a standalone mock application.
