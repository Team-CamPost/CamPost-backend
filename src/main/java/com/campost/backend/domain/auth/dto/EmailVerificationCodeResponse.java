package com.campost.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "이메일 인증번호 전송 응답")
public record EmailVerificationCodeResponse(
        @Schema(description = "이메일", example = "campost@example.com")
        String email,

        @Schema(description = "인증번호 만료 시각")
        OffsetDateTime expiresAt
) {
}
