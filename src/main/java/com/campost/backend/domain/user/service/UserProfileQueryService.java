package com.campost.backend.domain.user.service;

import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.exception.UserNotFoundException;
import com.campost.backend.domain.user.model.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileQueryService {

    private final UserRepository userRepository;

    public UserProfileQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfile getProfile(long userId) {
        return userRepository.findProfileById(userId)
                .orElseThrow(UserNotFoundException::new);
    }
}
