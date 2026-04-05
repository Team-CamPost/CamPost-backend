package com.campost.backend.global.api;

public record ErrorResponse(
        boolean isSuccess,
        String code,
    String message
) {
    public static ErrorResponse of(ApiCode code) {
        return of(code, code.message());
    }

    public static ErrorResponse of(ApiCode code, String message) {
        return new ErrorResponse(false, code.code(), message);
    }
}
