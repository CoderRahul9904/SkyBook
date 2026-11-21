package com.skybook.admin.dto;

import com.skybook.flight.FlightStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

public record CreateFlightRequest(
        @NotBlank String airlineName,
        @NotBlank String airlineCode,
        @NotBlank String flightNumber,
        @NotNull @Positive Long originAirportId,
        @NotNull @Positive Long destinationAirportId,
        @NotNull OffsetDateTime departureTime,
        @NotNull OffsetDateTime arrivalTime,
        FlightStatus status
) {
}
