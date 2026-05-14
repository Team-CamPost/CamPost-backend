package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.LoginRequest;
import com.campost.backend.domain.auth.dto.LoginResponse;
import com.campost.backend.domain.auth.exception.BadCredentialsException;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.global.jwt.JwtTokenService;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordHashService passwordHashService;
    private final JwtTokenService jwtTokenService;

    public LoginService(
            UserRepository userRepository,
            PasswordHashService passwordHashService,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordHashService = passwordHashService;
        this.jwtTokenService = jwtTokenService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(BadCredentialsException::new);

        if (!passwordHashService.matches(request.password(), user.passwordHash())) {
            throw new BadCredentialsException();
        }

        String token = jwtTokenService.generate(user.id(), user.username(), user.role());
        return LoginResponse.of(token);
    }
}
