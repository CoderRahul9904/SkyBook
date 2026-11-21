package com.skybook.admin.dto;

import com.skybook.flight.FlightStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record AdminFlightResponse(
        Long id,
        String airlineName,
        String airlineCode,
        String flightNumber,
        AirportResponse origin,
        AirportResponse destination,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime,
        FlightStatus status,
        List<CabinManagementResponse> cabins
) {
}
