package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.SignupRequest;
import com.campost.backend.domain.auth.exception.DuplicatedEmailException;
import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicatedEmailException();
        }

        String passwordHash = passwordHashService.hash(request.password());

        return userRepository.save(new SignupUserCreateCommand(
                request.username(),
                request.email(),
                passwordHash
        ));
    }
}
