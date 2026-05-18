package com.campost.backend.domain.user.service;

import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.dto.OnboardingProfileRequest;
import com.campost.backend.domain.user.exception.UserNotFoundException;
import com.campost.backend.domain.user.model.UserOnboardingProfile;
import com.campost.backend.domain.user.model.UserOnboardingProfileUpdateCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserOnboardingProfileService {

    private final UserRepository userRepository;

    public UserOnboardingProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserOnboardingProfile saveProfile(long userId, OnboardingProfileRequest request) {
        UserOnboardingProfileUpdateCommand command = new UserOnboardingProfileUpdateCommand(
                userId,
                request.department().trim(),
                request.grade(),
                request.nickname().trim()
        );

        return userRepository.updateOnboardingProfile(command)
                .orElseThrow(UserNotFoundException::new);
    }
}
