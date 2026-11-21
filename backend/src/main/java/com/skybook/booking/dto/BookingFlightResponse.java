package com.skybook.booking.dto;

import java.time.OffsetDateTime;

public record BookingFlightResponse(
        Long id,
        String airlineName,
        String airlineCode,
        String flightNumber,
        String originCode,
        String destinationCode,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime
) {
}
