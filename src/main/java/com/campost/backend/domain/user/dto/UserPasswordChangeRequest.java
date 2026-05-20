package com.campost.backend.domain.user.dto;

import com.campost.backend.domain.auth.validation.AuthValidationRules;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "사용자 비밀번호 변경 요청")
public record UserPasswordChangeRequest(
        @Schema(description = "현재 비밀번호", example = "password123")
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @Schema(description = "새 비밀번호", example = "newPassword123")
        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Pattern(
                regexp = AuthValidationRules.PASSWORD_PATTERN,
                message = AuthValidationRules.PASSWORD_MESSAGE
        )
        String newPassword
) {
}
