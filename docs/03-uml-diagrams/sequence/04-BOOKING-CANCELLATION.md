# Sequence Diagram — Booking Cancellation

## Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    actor Customer
    participant UI as React Frontend
    participant Controller as BookingController
    participant Security as JWT Security Context
    participant BookingService as BookingService
    participant BookingRepo as BookingRepository
    participant InventoryRepo as FlightInventoryRepository
    participant DB as PostgreSQL

    Customer->>UI: Request cancellation for PNR
    UI->>Controller: PATCH /api/v1/bookings/{pnr}/cancel
    Controller->>Security: Get authenticated customer identity
    Security-->>Controller: Authenticated user ID and CUSTOMER role

    alt Missing or invalid authentication
        Controller-->>UI: Unauthorized response
    else Authenticated CUSTOMER
        Controller->>BookingService: cancelBooking(userId, pnr)
        BookingService->>BookingRepo: Find booking by PNR
        BookingRepo->>DB: Read booking, flight and cabin
        DB-->>BookingRepo: Booking or no record

        alt Booking not found or not owned by customer
            BookingService-->>Controller: Unauthorized lookup/cancellation
            Controller-->>UI: Access denied without leaking ownership
            UI-->>Customer: Booking could not be cancelled
        else Booking owned by customer

            alt Booking already CANCELLED
                BookingService-->>Controller: Already cancelled
                Controller-->>UI: Repeated cancellation response
                UI-->>Customer: Booking is already cancelled
            else Requested at or within two hours of departure
                BookingService-->>Controller: Cancellation cutoff exceeded
                Controller-->>UI: Ineligible cancellation response
                UI-->>Customer: Cancellation is no longer allowed
            else Eligible confirmed booking
                BookingService->>DB: Begin transaction
                BookingService->>InventoryRepo: Read cabin inventory and lock record
                InventoryRepo->>DB: Read/lock inventory
                DB-->>InventoryRepo: Inventory record

                alt Update or restoration failure
                    BookingService->>DB: Roll back booking and inventory changes
                    DB-->>BookingService: Rolled back
                    BookingService-->>Controller: Cancellation transaction failure
                    Controller-->>UI: Cancellation failed
                    UI-->>Customer: Booking remains unchanged
                else Transaction can proceed
                    BookingService->>BookingRepo: Set booking status to CANCELLED
                    BookingRepo->>DB: Update booking once
                    BookingService->>InventoryRepo: Restore passengerCount seats exactly once
                    InventoryRepo->>DB: Increase available seats atomically
                    DB-->>BookingService: Booking and inventory persisted
                    BookingService->>DB: Commit transaction
                    BookingService-->>Controller: Cancelled Booking
                    Controller-->>UI: Cancellation success
                    UI-->>Customer: Show cancelled status and confirmation
                end
            end
        end
    end
```

## Workflow Explanation

Cancellation is processed by PNR through `BookingService`. The service derives customer identity from security context, verifies booking ownership, detects repeated cancellation and evaluates the two-hour departure cutoff. For an eligible booking, status update and cabin-seat restoration execute as one transaction. If any persistence step fails, both changes roll back. Repeated and cutoff-ineligible attempts never restore inventory.

## Consistency and Assumptions

- Uses the approved `/api/v1/bookings/{pnr}/cancel` operation.
- Booking remains retrievable with `CANCELLED` status after successful cancellation.
- Cutoff timezone interpretation remains an SRS open question.
- Inventory restoration uses the booking's passenger count, cabin class and fare snapshot relationship.
