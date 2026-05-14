package com.campost.backend.domain.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        String name
) {
    public static LoginResponse of(String accessToken, String name) {
        return new LoginResponse(accessToken, "Bearer", name);
    }
}
