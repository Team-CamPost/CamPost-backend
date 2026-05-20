package com.campost.backend.domain.user.model;

public record UserProfileUpdateCommand(
        long userId,
        String department,
        Integer grade,
        String nickname
) {
}
