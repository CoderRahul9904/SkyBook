package com.skybook.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CabinInventoryRequest(
        @NotNull @Min(0) Integer totalSeats,
        @NotNull @Min(0) Integer availableSeats
) {
}
