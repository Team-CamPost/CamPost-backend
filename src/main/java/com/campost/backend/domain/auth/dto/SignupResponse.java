package com.campost.backend.domain.auth.dto;

import com.campost.backend.domain.auth.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답")
public record SignupResponse(
        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "아이디", example = "campost123")
        String username,

        @Schema(description = "이메일", example = "campost@example.com")
        String email
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.name(),
                user.username(),
                user.email()
        );
    }
}
