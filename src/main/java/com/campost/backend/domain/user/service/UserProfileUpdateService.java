package com.campost.backend.domain.user.service;

import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.dto.UserProfileUpdateRequest;
import com.campost.backend.domain.user.exception.UserNotFoundException;
import com.campost.backend.domain.user.model.UserProfile;
import com.campost.backend.domain.user.model.UserProfileUpdateCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                request.department().trim(),
                request.grade(),
                request.nickname().trim()
        );

        return userRepository.updateProfile(command)
                .orElseThrow(UserNotFoundException::new);
    }
}
