package com.skybook.common.response;

public record FieldErrorResponse(
        String field,
        String message
) {
}
