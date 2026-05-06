package com.campost.backend.domain.auth.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHashServiceTest {

    private final PasswordHashService passwordHashService = new PasswordHashService();

    @Test
    void hashDoesNotStoreRawPassword() {
        String rawPassword = "password123";

        String passwordHash = passwordHashService.hash(rawPassword);

        assertThat(passwordHash).isNotEqualTo(rawPassword);
        assertThat(passwordHash).doesNotContain(rawPassword);
        assertThat(passwordHash).startsWith("pbkdf2_sha256$");
    }

    @Test
    void matchesReturnsTrueForOriginalPassword() {
        String rawPassword = "password123";
        String passwordHash = passwordHashService.hash(rawPassword);

        assertThat(passwordHashService.matches(rawPassword, passwordHash)).isTrue();
        assertThat(passwordHashService.matches("wrongPassword123", passwordHash)).isFalse();
    }
}
