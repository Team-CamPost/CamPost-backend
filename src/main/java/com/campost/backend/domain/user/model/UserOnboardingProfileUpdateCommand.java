package com.campost.backend.domain.user.model;

public record UserOnboardingProfileUpdateCommand(
        long userId,
        String department,
        int grade,
        String nickname
) {
}
