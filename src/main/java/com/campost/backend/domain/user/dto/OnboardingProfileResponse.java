package com.campost.backend.domain.user.dto;

import com.campost.backend.domain.user.model.UserOnboardingProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "첫 로그인 온보딩 프로필 저장 응답")
public record OnboardingProfileResponse(
        @Schema(description = "사용자 ID", example = "1")
        long userId,

        @Schema(description = "학과 코드", example = "SW")
        String department,

        @Schema(description = "학년", example = "3")
        int grade,

        @Schema(description = "닉네임", example = "캠포스트유저")
        String nickname,

        @Schema(description = "프로필 설정 완료 여부", example = "true")
        boolean profileCompleted
) {
    public static OnboardingProfileResponse from(UserOnboardingProfile profile) {
        return new OnboardingProfileResponse(
                profile.userId(),
                profile.department(),
                profile.grade(),
                profile.nickname(),
                profile.profileCompleted()
        );
    }
}
