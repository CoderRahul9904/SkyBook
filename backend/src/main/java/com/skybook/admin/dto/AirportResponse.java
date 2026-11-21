package com.skybook.admin.dto;

import java.time.OffsetDateTime;

public record AirportResponse(
        Long id,
        String code,
        String name,
        String city,
        String country,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
