package com.skybook.booking.dto;

import com.skybook.flight.CabinClass;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateBookingRequest(
        @NotNull
        Long flightId,

        @NotNull
        CabinClass cabinClass,

        @NotEmpty
        List<@Valid PassengerRequest> passengers
) {
}
