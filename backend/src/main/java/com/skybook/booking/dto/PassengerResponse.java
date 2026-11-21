package com.skybook.booking.dto;

import java.time.LocalDate;

public record PassengerResponse(
        String fullName,
        LocalDate birthDate
) {
}
