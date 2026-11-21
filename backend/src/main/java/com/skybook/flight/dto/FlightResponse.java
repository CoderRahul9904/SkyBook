package com.skybook.flight.dto;

import com.skybook.flight.CabinClass;
import com.skybook.flight.FlightStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record FlightResponse(
        Long id,
        String airlineName,
        String airlineCode,
        String flightNumber,
        AirportResponse origin,
        AirportResponse destination,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime,
        long durationMinutes,
        int stops,
        CabinClass cabinClass,
        BigDecimal fare,
        int availableSeats,
        FlightStatus status,
        List<CabinAvailabilityResponse> cabins
) {
}
