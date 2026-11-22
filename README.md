# SkyBook — AI-Assisted Airline Booking Platform

SkyBook is a full-stack airline booking application with customer self-service flows, an administrator console, JWT authentication, PostgreSQL persistence, and an AI travel assistant named **AeroMate**.

The project is implemented as a modular monolith: a Spring Boot REST API in `backend/` and a React + TypeScript single-page application in `frontend/`.

## Features

### Customer

- Register and log in with JWT authentication
- Search flights by route and departure date
- Select a flight and cabin class
- Enter passenger details and create a booking
- View booking confirmation and PNR details
- Review and cancel existing bookings
- Look up bookings by PNR
- Chat with AeroMate for airline-policy and flight assistance

### Administrator

- Create and update airports
- Create and update flights
- Update fares and seat inventory by cabin class
- View cabin availability snapshots
- List and cancel bookings
- Look up bookings by PNR

### AeroMate AI

- RAG over airline policy and FAQ documents
- Read-only Spring AI tool calling for flight search and booking lookup
- Persistent JDBC chat memory with conversation ownership checks
- MCP-style JSON-RPC demonstration endpoints
- Explicit backend validation for all booking-related actions

## Technology Stack

### Backend

- Java 21
- Spring Boot 4.1.2
- Spring Web / MVC
- Spring Security
- Spring Data JPA and Hibernate
- Spring AI 2.0.0
- PostgreSQL / pgvector
- JWT with JJWT
- Bean Validation
- Maven Wrapper
- OpenAPI / Swagger UI

### Frontend

- React 19
- TypeScript
- Vite 8
- React Router DOM
- Tailwind CSS 4
- Axios
- React Hook Form
- Zod
- Lucide React
- Vitest

## Repository Structure

```text
backend/
  src/main/java/com/skybook/
    admin/       airport, flight, fare, inventory, and booking administration
    ai/          AeroMate, RAG, chat memory, and MCP-style endpoints
    airport/     airport domain and API
    auth/        registration, login, JWT, and security
    booking/     passenger, booking, PNR, and cancellation workflows
    common/      shared API responses and exception handling
    config/      application, security, and AI configuration
    flight/      flight search, details, fares, and inventory
  src/test/java/  automated backend tests
  pom.xml         Maven dependencies and build configuration

frontend/
  src/
    app/         authentication context, providers, and routes
    components/  reusable UI and layout components
    pages/       customer, admin, and AeroMate screens
    services/api Axios client and API modules
    types/       shared TypeScript contracts
  package.json    scripts and dependencies

docs/
  01-project-proposal/
  02-srs/
  03-uml-diagrams/
  04-er-diagram/
  05-database-documentation/
  AI_INTEGRATION.md
  TESTING.md
  PHASE7_HANDOFF.md
```

## How It Works

React routes in `frontend/src/app/router.tsx` send requests through the Axios modules in `frontend/src/services/api`. The Spring Boot API is organized by domain modules under `backend/src/main/java/com/skybook`, with controllers handling HTTP contracts and service/repository layers implementing validation and persistence.

AeroMate calls Spring AI chat features, retrieval advisors, and read-only tools. Chat messages are persisted with an authenticated owner ID, while the booking and flight tools reuse the backend services rather than bypassing business validation.

## Prerequisites

- Java 21+
- Maven 3.9+ or the included Maven Wrapper
- Node.js 20+ and npm
- PostgreSQL
- Optional: an OpenAI-compatible AI API key for live AeroMate responses

## Backend Setup

```powershell
cd backend

# PowerShell: create the dev database variables
$env:SKYBOOK_DB_URL="jdbc:postgresql://localhost:5432/skybook"
$env:SKYBOOK_DB_USERNAME="postgres"
$env:SKYBOOK_DB_PASSWORD="your-password"

.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

The backend runs at `http://localhost:8080` by default. Swagger/OpenAPI is available when exposed by the local Spring configuration.

### AI configuration

Set these environment variables before starting the backend to enable live AI responses:

```powershell
$env:SKYBOOK_OPENAI_BASE_URL="https://api.openai.com"
$env:SKYBOOK_OPENAI_API_KEY="your-api-key"
$env:SKYBOOK_OPENAI_CHAT_MODEL="gpt-4o-mini"
```

The AI integration reads airline policy Markdown files from `backend/src/main/resources/airline-policies/` and uses PostgreSQL/pgvector where configured.

## Frontend Setup

```powershell
cd frontend
Copy-Item .env.example .env
npm install
npm run dev
```

Set the API base URL in `frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

The Vite development server normally runs at `http://localhost:5173`.

## Available Scripts

### Frontend

```text
npm run dev          Start the Vite development server
npm run build        Type-check and create a production build
npm run lint         Run ESLint
npm run type-check   Check TypeScript without emitting files
npm run test         Run the Vitest suite
npm run preview      Preview the production build
```

### Backend

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd clean test
.\mvnw.cmd clean compile
.\mvnw.cmd clean package -DskipTests
```

## Main API Routes

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/flights/search`
- `GET /api/v1/flights/{flightId}`
- `POST /api/v1/bookings`
- `GET /api/v1/bookings/my`
- `GET /api/v1/bookings/{pnr}`
- `PATCH /api/v1/bookings/{pnr}/cancel`
- `POST /api/v1/admin/airports`
- `POST /api/v1/admin/flights`
- `PUT /api/v1/admin/flights/{flightId}/fares/{cabinClass}`
- `PUT /api/v1/admin/flights/{flightId}/inventory/{cabinClass}`
- `POST /api/v1/ai/chat`
- `GET /api/v1/ai/conversations`
- `POST /api/v1/mcp/rpc`

Protected routes require a bearer JWT from `/api/v1/auth/login`.

## Testing

Backend test results are recorded in `docs/TESTING.md`. A typical verification run is:

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd clean package -DskipTests

cd ..\frontend
npm run test
npm run build
```

The backend suite includes AI service and security-focused tests. Live AI gateway and real embedding quality remain environment-dependent and require credentials and provider availability.

## Documentation

- [Backend guide](backend/README.md)
- [Frontend guide](frontend/README.md)
- [AI integration](docs/AI_INTEGRATION.md)
- [Testing report](docs/TESTING.md)
- [Phase 7 handoff](docs/PHASE7_HANDOFF.md)
- [Project proposal](docs/01-project-proposal)
- [Software requirements](docs/02-srs)
- [UML diagrams](docs/03-uml-diagrams)
- [ER diagram](docs/04-er-diagram)
- [Database documentation](docs/05-database-documentation)

## Security Notes

- JWTs are issued by the backend and stored by the frontend authentication context.
- Booking and conversation operations enforce authenticated ownership where applicable.
- AI tools are read-only and must not independently confirm bookings or mutate flight data.
- Never commit API keys, passwords, JWT secrets, or database credentials.
- Demonstration booking and payment behavior is not a real airline reservation or financial transaction.
