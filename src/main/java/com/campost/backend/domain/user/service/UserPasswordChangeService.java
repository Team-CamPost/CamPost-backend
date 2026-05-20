package com.campost.backend.domain.user.service;

import com.campost.backend.domain.auth.exception.BadCredentialsException;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.auth.service.PasswordHashService;
import com.campost.backend.domain.user.dto.UserPasswordChangeRequest;
import com.campost.backend.domain.user.exception.SamePasswordException;
import com.campost.backend.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPasswordChangeService {

    private final UserRepository userRepository;
    private final PasswordHashService passwordHashService;

    public UserPasswordChangeService(
            UserRepository userRepository,
            PasswordHashService passwordHashService
    ) {
        this.userRepository = userRepository;
        this.passwordHashService = passwordHashService;
    }

    @Transactional
    public void changePassword(long userId, UserPasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordHashService.matches(request.currentPassword(), user.passwordHash())) {
            throw new BadCredentialsException();
        }

        if (request.currentPassword().equals(request.newPassword())) {
            throw new SamePasswordException();
        }

        String newPasswordHash = passwordHashService.hash(request.newPassword());
        boolean updated = userRepository.updatePasswordHash(userId, newPasswordHash);

        if (!updated) {
            throw new UserNotFoundException();
        }
    }
}
