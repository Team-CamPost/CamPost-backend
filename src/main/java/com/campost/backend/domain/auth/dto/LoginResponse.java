package com.campost.backend.domain.auth.dto;

public record LoginResponse(
        long userId,
        String username,
        String name,
        String role,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshTokenExpiresIn
) {
    public static LoginResponse of(
            long userId,
            String username,
            String name,
            String role,
            String accessToken,
            String refreshToken,
            long expiresIn,
            long refreshTokenExpiresIn
    ) {
        return new LoginResponse(
                userId,
                username,
                name,
                role,
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                refreshTokenExpiresIn
        );
    }
}
