package com.campost.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "아이디 중복 확인 응답")
public record UsernameAvailabilityResponse(
        @Schema(description = "아이디", example = "campost123")
        String username,

        @Schema(description = "사용 가능 여부", example = "true")
        boolean available
) {
}
