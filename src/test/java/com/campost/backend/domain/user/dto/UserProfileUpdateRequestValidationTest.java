package com.campost.backend.domain.user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileUpdateRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validatesRequiredFields() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("", null, "");

        assertThat(validator.validate(request)).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void validatesDepartmentGradeAndNicknameRules() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "UNKNOWN",
                7,
                "a".repeat(51)
        );

        assertThat(validator.validate(request)).hasSize(3);
    }

    @Test
    void acceptsValidRequest() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("SW", 3, "캠포스트유저");

        assertThat(validator.validate(request)).isEmpty();
    }
}
