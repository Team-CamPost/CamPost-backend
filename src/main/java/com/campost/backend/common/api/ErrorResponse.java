package com.campost.backend.common.api;

import java.time.OffsetDateTime;

public record ErrorResponse(
        boolean success,
        String message,
        OffsetDateTime timestamp
) {
    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, message, OffsetDateTime.now());
    }
}
