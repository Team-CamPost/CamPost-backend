package com.campost.backend.domain.auth.dto;

import com.campost.backend.domain.auth.validation.AuthValidationRules;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignupRequest(
        @Schema(description = "아이디", example = "campost123")
        @NotBlank(message = "아이디를 입력해주세요.")
        @Pattern(
                regexp = AuthValidationRules.USERNAME_PATTERN,
                message = AuthValidationRules.USERNAME_MESSAGE
        )
        String username,

        @Schema(description = "이메일", example = "campost@example.com")
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식으로 입력해주세요.")
        @Size(max = 255, message = "이메일은 255자 이하로 입력해주세요.")
        String email,

        @Schema(description = "비밀번호", example = "password123")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(
                regexp = AuthValidationRules.PASSWORD_PATTERN,
                message = AuthValidationRules.PASSWORD_MESSAGE
        )
        String password
) {
}
