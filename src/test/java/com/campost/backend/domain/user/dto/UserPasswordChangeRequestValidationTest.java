package com.campost.backend.domain.user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPasswordChangeRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validatesRequiredFields() {
        UserPasswordChangeRequest request = new UserPasswordChangeRequest("", "");

        assertThat(validator.validate(request)).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void validatesNewPasswordRules() {
        UserPasswordChangeRequest request = new UserPasswordChangeRequest(
                "password123",
                "password"
        );

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("newPassword"));
    }

    @Test
    void acceptsValidRequest() {
        UserPasswordChangeRequest request = new UserPasswordChangeRequest(
                "password123",
                "newPassword123"
        );

        assertThat(validator.validate(request)).isEmpty();
    }
}
