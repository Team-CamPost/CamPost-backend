package com.campost.backend.domain.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignupRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validSignupRequestPassesValidation() {
        SignupRequest request = new SignupRequest(
                "campost123",
                "campost@example.com",
                "password123",
                "캠포스트"
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void blankRequiredFieldsFailValidation() {
        SignupRequest request = new SignupRequest(
                "",
                "",
                "",
                ""
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("username", "email", "password", "nickname");
    }

    @Test
    void invalidEmailFormatFailsValidation() {
        SignupRequest request = new SignupRequest(
                "campost123",
                "invalid-email",
                "password123",
                "캠포스트"
        );

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("email"));
    }

    @Test
    void passwordWithoutLetterAndNumberFailsValidation() {
        SignupRequest request = new SignupRequest(
                "campost123",
                "campost@example.com",
                "password",
                "캠포스트"
        );

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("password"));
    }

    @Test
    void shortPasswordFailsValidation() {
        SignupRequest request = new SignupRequest(
                "campost123",
                "campost@example.com",
                "abc123",
                "캠포스트"
        );

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("password"));
    }
}
