package com.skybook.auth.dto;

import com.skybook.auth.UserRole;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        UserRole role
) {
}
