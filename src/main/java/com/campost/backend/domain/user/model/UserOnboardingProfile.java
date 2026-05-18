package com.campost.backend.domain.user.model;

public record UserOnboardingProfile(
        long userId,
        String department,
        int grade,
        String nickname,
        boolean profileCompleted
) {
}
