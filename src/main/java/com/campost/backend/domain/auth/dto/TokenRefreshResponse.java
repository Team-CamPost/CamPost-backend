package com.campost.backend.domain.auth.dto;

public record TokenRefreshResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public static TokenRefreshResponse of(String accessToken, long expiresIn) {
        return new TokenRefreshResponse(accessToken, "Bearer", expiresIn);
    }
}
