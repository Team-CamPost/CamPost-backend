package com.campost.backend.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"isSuccess", "code", "message"})
public record ErrorResponse(
    @JsonProperty("isSuccess")
        boolean isSuccess,
    @JsonProperty("code")
        String code,
    @JsonProperty("message")
        String message
) {
    public static ErrorResponse of(ApiCode code) {
        return of(code, code.message());
    }

    public static ErrorResponse of(ApiCode code, String message) {
        return new ErrorResponse(false, code.code(), message);
    }
}
