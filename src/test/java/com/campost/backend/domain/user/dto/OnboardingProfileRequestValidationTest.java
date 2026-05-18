package com.campost.backend.domain.user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OnboardingProfileRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validatesRequiredFields() {
        OnboardingProfileRequest request = new OnboardingProfileRequest("", null, "");

        assertThat(validator.validate(request)).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void validatesDepartmentGradeAndNicknameRules() {
        OnboardingProfileRequest request = new OnboardingProfileRequest(
                "UNKNOWN",
                7,
                "a".repeat(51)
        );

        assertThat(validator.validate(request)).hasSize(3);
    }

    @Test
    void acceptsValidRequest() {
        OnboardingProfileRequest request = new OnboardingProfileRequest("SW", 3, "캠포스트유저");

        assertThat(validator.validate(request)).isEmpty();
    }
}
