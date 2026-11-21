package com.skybook.common.response;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
        OffsetDateTime timestamp,
        int status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> of(int status, String message, T data) {
        return new ApiResponse<>(OffsetDateTime.now(), status, message, data);
    }
}
