package com.campost.backend.domain.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenRefreshRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void refreshTokenIsRequired() {
        TokenRefreshRequest request = new TokenRefreshRequest("");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void validRequestPassesValidation() {
        TokenRefreshRequest request = new TokenRefreshRequest("refresh-token");

        assertThat(validator.validate(request)).isEmpty();
    }
}
