package com.campost.backend.domain.auth.model;

import java.time.OffsetDateTime;

public record User(
        long id,
        String username,
        String email,
        String passwordHash,
        String role,
        OffsetDateTime createdAt
) {
}
