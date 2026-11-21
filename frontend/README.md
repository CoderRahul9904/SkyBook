# SkyBook Frontend (Phase 8)

React + TypeScript + Vite + Tailwind frontend integrated with the SkyBook Spring Boot backend.

## Stack

- React
- TypeScript
- Vite
- Tailwind CSS
- React Router
- Axios
- React Hook Form + Zod
- Lucide React

## Environment

Create `.env` from `.env.example`:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Scripts

From `frontend/`:

```bash
npm install
npm run dev
npm run lint
npm run type-check
npm run test
npm run build
```

## Implemented Features

### Customer

- Register and login with JWT-based auth
- Protected routes
- Flight search and flight selection
- Passenger details form and booking creation
- Booking confirmation via PNR details page
- My bookings list
- PNR lookup and booking detail page
- Booking cancellation with confirmation

### Admin

- Airport create/update
- Flight create/update
- Fare update by cabin class
- Inventory update by cabin class
- Cabin snapshot per flight
- Booking list and admin cancellation

### AeroMate

- Start chat conversation
- Continue existing conversation
- View conversation history list
- Load conversation messages
- Retry last failed message

## Backend Endpoints Integrated

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/flights/search`
- `GET /api/v1/flights/{flightId}`
- `POST /api/v1/bookings`
- `GET /api/v1/bookings/my`
- `GET /api/v1/bookings/{pnr}`
- `PATCH /api/v1/bookings/{pnr}/cancel`
- `POST /api/v1/ai/chat`
- `GET /api/v1/ai/conversations`
- `GET /api/v1/ai/conversations/{conversationId}`
- `POST /api/v1/admin/airports`
- `GET /api/v1/admin/airports`
- `PUT /api/v1/admin/airports/{id}`
- `POST /api/v1/admin/flights`
- `GET /api/v1/admin/flights`
- `PUT /api/v1/admin/flights/{id}`
- `PUT /api/v1/admin/flights/{flightId}/fares/{cabinClass}`
- `PUT /api/v1/admin/flights/{flightId}/inventory/{cabinClass}`
- `GET /api/v1/admin/flights/{flightId}/cabins`
- `GET /api/v1/admin/bookings`
- `GET /api/v1/admin/bookings/{pnr}`
- `PATCH /api/v1/admin/bookings/{pnr}/cancel`

## Notes

- API contracts are based on current backend controllers and DTOs.
- No mocked success flows are used for booking/admin/AI operations.
- MCP endpoint exists in backend but is not directly used by browser UI, as per phase scope.
