package com.campost.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증번호 확인 요청")
public record EmailVerificationCheckRequest(
        @Schema(description = "이메일", example = "campost@example.com")
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식으로 입력해주세요.")
        @Size(max = 255, message = "이메일은 255자 이하로 입력해주세요.")
        String email,

        @Schema(description = "6자리 인증번호", example = "123456")
        @NotBlank(message = "이메일 인증번호를 입력해주세요.")
        @Pattern(regexp = "^\\d{6}$", message = "이메일 인증번호는 6자리 숫자로 입력해주세요.")
        String code
) {
}
