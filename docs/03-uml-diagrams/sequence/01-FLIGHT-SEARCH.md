# Sequence Diagram — Flight Search

## Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    actor Customer
    participant UI as React Frontend
    participant Controller as FlightController
    participant Service as FlightService
    participant Criteria as FlightSearchCriteria
    participant FlightRepo as FlightRepository
    participant InventoryRepo as FlightInventoryRepository
    participant FareRepo as FlightFareRepository
    participant DB as PostgreSQL

    Customer->>UI: Enter origin, destination, date,<br/>passengers and cabin class
    UI->>Controller: GET /api/v1/flights/search
    Controller->>Criteria: Build and validate criteria

    alt Invalid origin, destination, date,<br/>passenger count or cabin class
        Criteria-->>Controller: Validation failure
        Controller-->>UI: Field validation response
        UI-->>Customer: Display validation error
    else Valid search criteria
        Criteria-->>Controller: Valid criteria
        Controller->>Service: search(criteria)
        Service->>FlightRepo: Find direct flights for route and date
        FlightRepo->>DB: Query flights and airports
        DB-->>FlightRepo: Matching flights

        loop For each matching flight
            Service->>InventoryRepo: Find inventory by flight and cabin
            InventoryRepo->>DB: Read cabin availability
            DB-->>InventoryRepo: Inventory record
            InventoryRepo-->>Service: Availability

            alt Available seats >= passenger count
                Service->>FareRepo: Find cabin fare
                FareRepo->>DB: Read flight fare
                DB-->>FareRepo: Fare record
                FareRepo-->>Service: BigDecimal fare
                Service->>Service: Add direct flight result with 0 stops
            else Insufficient cabin availability
                Service->>Service: Exclude flight
            end
        end

        alt One or more eligible flights
            Service-->>Controller: List<FlightSearchResult>
            Controller-->>UI: Search results
            UI-->>Customer: Display flights, fares and availability
        else No eligible flights
            Service-->>Controller: Empty result list
            Controller-->>UI: Empty result
            UI-->>Customer: Display no matching flights
        end
    end
```

## Workflow Explanation

The Customer submits validated frontend search fields to the flight search controller. The backend constructs a `FlightSearchCriteria` value and rejects invalid combinations before querying the database. `FlightService` retrieves direct flights and independently checks cabin inventory and fare records. Flights with insufficient seat availability are omitted, and valid direct flights are identified with zero stops. The frontend displays loading, populated, empty or validation/error states without introducing the workflow through an external airline system.

## Consistency and Assumptions

- Uses the approved `/api/v1/flights/search` operation.
- Applies only direct, one-way flights from PostgreSQL.
- No real airline/GDS search occurs.
- The exact future-date boundary remains an open SRS question.
- Monetary fare values are represented with `BigDecimal` semantics.
