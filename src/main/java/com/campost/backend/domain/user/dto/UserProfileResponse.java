package com.campost.backend.domain.user.dto;

import com.campost.backend.domain.user.model.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "내 정보 조회 응답")
public record UserProfileResponse(
        @Schema(description = "사용자 ID", example = "1")
        long userId,

        @Schema(description = "아이디", example = "campost123")
        String username,

        @Schema(description = "이메일", example = "campost@example.com")
        String email,

        @Schema(description = "닉네임", example = "캠포스트유저")
        String nickname,

        @Schema(description = "학과 코드", example = "SW")
        String department,

        @Schema(description = "학년", example = "3")
        Integer grade,

        @Schema(description = "권한", example = "GUEST")
        String role,

        @Schema(description = "프로필 설정 완료 여부", example = "true")
        boolean profileCompleted,

        @Schema(description = "가입 일시")
        OffsetDateTime createdAt,

        @Schema(description = "마지막 로그인 일시")
        OffsetDateTime lastLoginAt
) {
    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
                profile.userId(),
                profile.username(),
                profile.email(),
                profile.nickname(),
                profile.department(),
                profile.grade(),
                profile.role(),
                profile.profileCompleted(),
                profile.createdAt(),
                profile.lastLoginAt()
        );
    }
}
