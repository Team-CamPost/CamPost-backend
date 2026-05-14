package com.campost.backend.domain.auth.model;

public record SignupUserCreateCommand(
        String name,
        String username,
        String email,
        String passwordHash
) {
}
