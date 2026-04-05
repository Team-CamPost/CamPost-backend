package com.campost.backend.global.api;

public record ApiResponse<T>(
        boolean isSuccess,
        String code,
        String message,
        T result
) {
    public static <T> ApiResponse<T> ok(T result) {
        return ok(ApiCode.COMMON200, result);
    }

    public static <T> ApiResponse<T> ok(ApiCode code, T result) {
        return new ApiResponse<>(true, code.code(), code.message(), result);
    }
}
