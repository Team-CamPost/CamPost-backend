package com.campost.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "이메일 인증번호 확인 응답")
public record EmailVerificationCheckResponse(
        @Schema(description = "이메일", example = "campost@example.com")
        String email,

        @Schema(description = "이메일 인증 완료 여부", example = "true")
        boolean verified,

        @Schema(description = "이메일 인증 완료 시각")
        OffsetDateTime verifiedAt
) {
}
