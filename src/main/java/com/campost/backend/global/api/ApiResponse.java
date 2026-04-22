package com.campost.backend.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public record ApiResponse<T>(
    @JsonProperty("isSuccess")
        boolean isSuccess,
    @JsonProperty("code")
        String code,
    @JsonProperty("message")
        String message,
    @JsonProperty("result")
        T result
) {
    public static <T> ApiResponse<T> ok(T result) {
        return ok(ApiCode.COMMON200, result);
    }

    public static <T> ApiResponse<T> ok(ApiCode code, T result) {
        return new ApiResponse<>(true, code.code(), code.message(), result);
    }
}
