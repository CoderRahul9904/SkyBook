package com.skybook.flight.dto;

import com.skybook.flight.CabinClass;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FlightSearchRequest(
        @NotBlank String origin,
        @NotBlank String destination,
        @NotNull LocalDate travelDate,
        @Min(1) int passengerCount,
        @NotNull CabinClass cabinClass
) {
}
