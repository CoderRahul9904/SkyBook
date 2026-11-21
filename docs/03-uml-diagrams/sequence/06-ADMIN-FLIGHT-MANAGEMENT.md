# Sequence Diagram — Admin Flight Management

## Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    actor Admin
    participant UI as React Frontend
    participant Controller as AdminController
    participant Security as JWT Security Context
    participant AdminService as AdminService
    participant AirportRepo as AirportRepository
    participant FlightRepo as FlightRepository
    participant FareRepo as FlightFareRepository
    participant InventoryRepo as FlightInventoryRepository
    participant DB as PostgreSQL

    Admin->>UI: Enter flight details, fare and inventory data
    UI->>Controller: POST or PUT /api/v1/admin/flights<br/>and related approved fare/inventory operations
    Controller->>Security: Validate JWT and ADMIN role

    alt Missing authentication or role is not ADMIN
        Security-->>Controller: Unauthorized or forbidden
        Controller-->>UI: Access denied
        UI-->>Admin: Display authorization error
    else Authenticated ADMIN
        Security-->>Controller: ADMIN identity
        Controller->>AdminService: Create or update flight and related data

        AdminService->>AirportRepo: Validate origin and destination airports
        AirportRepo->>DB: Read airport records
        DB-->>AirportRepo: Airports or no record

        alt Airports missing or origin equals destination
            AdminService-->>Controller: Invalid flight route
            Controller-->>UI: Route validation error
        else Valid route

            alt Create flight
                AdminService->>FlightRepo: Create flight schedule
                FlightRepo->>DB: Insert flight
                DB-->>FlightRepo: Flight or constraint failure
            else Update flight
                AdminService->>FlightRepo: Update existing flight
                FlightRepo->>DB: Update flight schedule
                DB-->>FlightRepo: Updated flight or constraint failure
            end

            alt Flight persistence failed
                FlightRepo-->>AdminService: Constraint or persistence failure
                AdminService-->>Controller: Flight operation failure
                Controller-->>UI: Error response
            else Flight persisted

                opt Fare data submitted
                    AdminService->>FareRepo: Create or update fare by cabin class
                    FareRepo->>DB: Insert or update fare record
                    DB-->>FareRepo: Fare persisted or constraint failure

                    alt Fare invalid or constraint failure
                        FareRepo-->>AdminService: Fare failure
                        AdminService-->>Controller: Fare validation failure
                        Controller-->>UI: Fare error
                    end
                end

                opt Inventory data submitted
                    AdminService->>InventoryRepo: Create or update inventory by cabin class
                    InventoryRepo->>DB: Insert or update total and available seats

                    alt Available seats negative or exceed total seats
                        DB-->>InventoryRepo: Constraint or validation failure
                        InventoryRepo-->>AdminService: Inventory failure
                        AdminService-->>Controller: Inventory validation failure
                        Controller-->>UI: Inventory error
                    else Valid inventory persisted
                        DB-->>InventoryRepo: Inventory persisted
                    end
                end

                AdminService-->>Controller: Flight management success
                Controller-->>UI: Updated flight details
                UI-->>Admin: Display success and flight summary
            end
        end
    end
```

## Workflow Explanation

The Admin submits flight management data through the authorized admin interface. The controller verifies JWT authentication and the ADMIN role before delegating to `AdminService`. The service validates existing origin and destination airports and requires them to differ. It then creates or updates the flight record and optionally creates or updates cabin-specific fares and inventory. Both inventory and fare operations enforce unique flight-and-cabin combinations and prevent invalid seat bounds.

The MVP does not define operational deletion of airport or flight records, so no delete workflow is represented.

## Consistency and Assumptions

- Covers approved create/update flight operations and cabin fare/inventory operations.
- Administrative endpoints require the ADMIN role.
- Exact supported cabin values remain open.
- Fare precision and rounding remain open.
- Flight, fare and inventory persistence details may require implementation-time transactions, but no more complex workflow is assumed.
