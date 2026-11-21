# Activity Diagram — Flight Booking

## PlantUML Activity Diagram

```plantuml
@startuml
|Customer|
start
:Select flight;
:Select cabin class;
:Enter full name and date of birth
for each passenger;
:Submit booking confirmation;

|React Frontend|
:Send booking request;

|JWT Security Context|
:Derive authenticated customer identity;

|BookingController|
if (Customer authenticated with CUSTOMER role?) then (yes)
  |BookingService|
  :Validate passenger count;
  :Validate passenger required fields;
  :Validate passenger birth dates;

  if (Passenger data valid?) then (yes)
    |FlightService|
    :Revalidate flight existence and status;
    :Revalidate cabin fare;
    :Lock or read cabin inventory;

    if (Flight and cabin selection valid?) then (yes)
      if (Available seats >= passenger count?) then (yes)
        :Begin database transaction;
        :Calculate fare from backend fare
        using passenger count;
        :Reserve seats atomically
        with concurrency control;

        if (Seat reservation successful?) then (yes)
          :Generate unique PNR;
          :Persist Booking with fare snapshot,
          cabin, passenger count and status;
          repeat
            :Persist BookingPassenger;
          repeat while (Another passenger?) is (yes)
          ->no;

          if (All booking and passenger writes succeed?) then (yes)
            :Commit transaction;
            |React Frontend|
            :Display PNR and booking confirmation;
          else (no)
            :Roll back transaction;
            |React Frontend|
            :Display transaction failure;
          endif
        else (no)
          :Roll back seat reservation;
          |React Frontend|
          :Display transaction or concurrency failure;
        endif
      else (no)
        |React Frontend|
        :Display insufficient inventory;
      endif
    else (no)
      :Return flight or cabin validation failure;
      |React Frontend|
      :Display booking cannot be confirmed;
    endif
  else (no)
    |React Frontend|
    :Display passenger validation errors;
  endif
else (no)
  |React Frontend|
  :Prompt login or report unauthorized access;
endif

|Customer|
stop
@enduml
```

## Workflow Explanation

The Customer selects a flight and cabin, enters passenger details and confirms the booking. Authentication is verified first, followed by passenger validation. `FlightService` revalidates flight, fare and cabin availability before `BookingService` starts a transaction. The backend calculates fare and reserves seats; successful execution persists the booking and passenger records before generating/returning a unique PNR.

## Important Business Rules and Decision Points

- Passenger count must equal the number of passenger records.
- Full name and date of birth are mandatory for every passenger.
- The client-supplied fare is never trusted.
- Available seats must not become negative.
- Booking creation and seat reservation are transactional.
- Database concurrency protection prevents overselling.
- Failed persistence rolls back booking, passenger and inventory changes.
- Cancellation and payment behaviors are not triggered by the booking workflow.

## Unresolved Assumptions

- PNR format and uniqueness generation strategy are open.
- Supported cabin-class values are open.
- Fare precision and rounding rules are open.
- Passenger-name/date validation limits beyond mandatory fields are open.

## Consistency Verification

This activity matches the booking sequence and SRS requirements FR-SELECT-004 through FR-BOOKING-015. It contains no payment, seat-map or rescheduling behavior.
