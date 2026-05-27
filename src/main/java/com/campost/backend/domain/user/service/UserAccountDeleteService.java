package com.campost.backend.domain.user.service;

import com.campost.backend.domain.auth.exception.BadCredentialsException;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.auth.service.PasswordHashService;
import com.campost.backend.domain.user.dto.UserAccountDeleteRequest;
import com.campost.backend.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountDeleteService {

    private final UserRepository userRepository;
    private final PasswordHashService passwordHashService;

    public UserAccountDeleteService(
            UserRepository userRepository,
            PasswordHashService passwordHashService
    ) {
        this.userRepository = userRepository;
        this.passwordHashService = passwordHashService;
    }

    @Transactional
    public void deleteAccount(long userId, UserAccountDeleteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordHashService.matches(request.currentPassword(), user.passwordHash())) {
            throw new BadCredentialsException();
        }

        boolean deleted = userRepository.deleteById(userId);

        if (!deleted) {
            throw new UserNotFoundException();
        }
    }
}
