package com.skybook.booking.dto;

import com.skybook.booking.BookingStatus;
import com.skybook.flight.CabinClass;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        String pnr,
        BookingFlightResponse flight,
        CabinClass cabinClass,
        int passengerCount,
        BigDecimal fareSnapshot,
        BookingStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<PassengerResponse> passengers
) {
}
