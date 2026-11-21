# Sequence Diagram — Flight Booking

## Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    actor Customer
    participant UI as React Frontend
    participant Controller as BookingController
    participant Security as JWT Security Context
    participant BookingService as BookingService
    participant FlightService as FlightService
    participant FlightRepo as FlightRepository
    participant FareRepo as FlightFareRepository
    participant InventoryRepo as FlightInventoryRepository
    participant BookingRepo as BookingRepository
    participant PassengerRepo as BookingPassengerRepository
    participant PNR as PnrGenerator
    participant DB as PostgreSQL

    Customer->>UI: Select flight and cabin; enter passengers
    UI->>Controller: POST /api/v1/bookings
    Controller->>Security: Get authenticated customer identity
    Security-->>Controller: Authenticated user ID and CUSTOMER role

    alt Missing or invalid authentication
        Controller-->>UI: Unauthorized response
        UI-->>Customer: Prompt authentication
    else Authenticated CUSTOMER
        Controller->>BookingService: createBooking(userId, bookingRequest)

        BookingService->>BookingService: Validate passenger count and passenger records

        alt Invalid passenger data or count mismatch
            BookingService-->>Controller: Passenger validation failure
            Controller-->>UI: Validation response
            UI-->>Customer: Correct passenger details
        else Valid passenger data
            BookingService->>FlightService: validateSelection(flightId, cabin, count)
            FlightService->>FlightRepo: Lock or read selected flight
            FlightRepo->>DB: Read flight status and route
            DB-->>FlightRepo: Flight record

            FlightService->>FareRepo: Read fare for flight and cabin
            FareRepo->>DB: Read cabin fare
            DB-->>FareRepo: Backend fare record

            FlightService->>InventoryRepo: Lock or read cabin inventory
            InventoryRepo->>DB: Read available seats
            DB-->>InventoryRepo: Inventory record

            alt Flight, fare or inventory invalid
                FlightService-->>BookingService: Selection unavailable
                BookingService-->>Controller: Validation failure
                Controller-->>UI: Booking cannot be confirmed
                UI-->>Customer: Select another flight or cabin
            else Inventory insufficient
                InventoryRepo-->>FlightService: Available < passenger count
                FlightService-->>BookingService: Insufficient inventory
                BookingService-->>Controller: Insufficient inventory
                Controller-->>UI: Insufficient seat response
                UI-->>Customer: Display seat availability error
            else Inventory sufficient
                BookingService->>BookingService: Begin database transaction
                BookingService->>BookingService: Calculate fare * passengerCount
                BookingService->>InventoryRepo: Reserve seats with concurrency control
                InventoryRepo->>DB: Decrease available seats atomically

                alt Persistence or transaction failure
                    DB-->>InventoryRepo: Failure or constraint violation
                    InventoryRepo-->>BookingService: Transaction failure
                    BookingService->>DB: Roll back transaction
                    DB-->>BookingService: Rolled back
                    BookingService-->>Controller: Booking failure
                    Controller-->>UI: Transaction failure response
                    UI-->>Customer: Booking was not confirmed
                else Seats reserved successfully
                    BookingService->>PNR: generateUniquePnr()
                    PNR-->>BookingService: Unique PNR
                    BookingService->>BookingRepo: Save booking with fare snapshot
                    BookingRepo->>DB: Insert booking

                    loop Each passenger
                        BookingService->>PassengerRepo: Save booking passenger
                        PassengerRepo->>DB: Insert BookingPassenger
                    end

                    DB-->>BookingService: Booking and passengers persisted
                    BookingService->>DB: Commit transaction
                    BookingService-->>Controller: Confirmed Booking
                    Controller-->>UI: PNR and booking details
                    UI-->>Customer: Display confirmation and PNR
                end
            end
        end
    end
```

## Workflow Explanation

The booking controller derives the customer identity from the JWT security context. `BookingService` validates passenger count and data, while `FlightService` revalidates flight status, cabin fare and availability. Seat reservation and persistence occur within one transactional unit. A PNR is generated for successful confirmation, and passengers are persisted as `BookingPassenger` records. Invalid passengers, insufficient inventory, persistence errors or concurrency conflicts leave the booking unconfirmed and trigger rollback.

## Consistency and Assumptions

- Uses the approved `/api/v1/bookings` operation.
- Prevents overselling through locking or equivalent concurrency control.
- Fare is calculated only from backend fare records and uses `BigDecimal` semantics.
- PNR format and uniqueness implementation remain open decisions.
- The exact cabin-class values remain open.
