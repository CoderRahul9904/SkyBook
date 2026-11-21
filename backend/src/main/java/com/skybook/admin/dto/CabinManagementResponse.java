package com.skybook.admin.dto;

import com.skybook.flight.CabinClass;

import java.math.BigDecimal;

public record CabinManagementResponse(
        CabinClass cabinClass,
        BigDecimal fare,
        Integer totalSeats,
        Integer availableSeats
) {
}
