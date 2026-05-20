package com.campost.backend.domain.user.model;

import java.time.OffsetDateTime;

public record UserProfile(
        long userId,
        String username,
        String email,
        String nickname,
        String department,
        Integer grade,
        String role,
        boolean profileCompleted,
        OffsetDateTime createdAt,
        OffsetDateTime lastLoginAt
) {
}
