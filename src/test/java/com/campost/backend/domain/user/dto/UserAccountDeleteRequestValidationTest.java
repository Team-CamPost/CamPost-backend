package com.campost.backend.domain.user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserAccountDeleteRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void currentPasswordIsRequired() {
        UserAccountDeleteRequest request = new UserAccountDeleteRequest("");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void validRequestPassesValidation() {
        UserAccountDeleteRequest request = new UserAccountDeleteRequest("password123");

        assertThat(validator.validate(request)).isEmpty();
    }
}
