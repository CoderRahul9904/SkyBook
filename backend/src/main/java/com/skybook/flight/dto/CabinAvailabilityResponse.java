package com.skybook.flight.dto;

import com.skybook.flight.CabinClass;

import java.math.BigDecimal;

public record CabinAvailabilityResponse(
        CabinClass cabinClass,
        BigDecimal fare,
        int totalSeats,
        int availableSeats
) {
}
