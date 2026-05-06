package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.SignupRequest;
import com.campost.backend.domain.auth.exception.DuplicatedEmailException;
import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class SignupUserService {

    private final UserRepository userRepository;
    private final PasswordHashService passwordHashService;

    public SignupUserService(
            UserRepository userRepository,
            PasswordHashService passwordHashService
    ) {
        this.userRepository = userRepository;
        this.passwordHashService = passwordHashService;
    }

    @Transactional
    public User saveUser(SignupRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicatedEmailException();
        }

        String passwordHash = passwordHashService.hash(request.password());

        return userRepository.save(new SignupUserCreateCommand(
                request.username(),
                normalizedEmail,
                passwordHash
        ));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
