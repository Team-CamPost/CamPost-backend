package com.campost.backend.domain.user.service;

import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.dto.UserProfileUpdateRequest;
import com.campost.backend.domain.user.exception.UserNotFoundException;
import com.campost.backend.domain.user.model.UserProfile;
import com.campost.backend.domain.user.model.UserProfileUpdateCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UserProfileUpdateService {

    private final UserRepository userRepository;

    public UserProfileUpdateService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserProfile updateProfile(long userId, UserProfileUpdateRequest request) {
        UserProfileUpdateCommand command = new UserProfileUpdateCommand(
                userId,
                trimRequired(request.department(), "department"),
                request.grade(),
                trimRequired(request.nickname(), "nickname")
        );

        return userRepository.updateProfile(command)
                .orElseThrow(UserNotFoundException::new);
    }

    private String trimRequired(String value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null.").trim();
    }
}
