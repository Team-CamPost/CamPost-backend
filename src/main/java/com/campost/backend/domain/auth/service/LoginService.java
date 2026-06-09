package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.LoginRequest;
import com.campost.backend.domain.auth.dto.LoginResponse;
import com.campost.backend.domain.auth.exception.BadCredentialsException;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.model.UserProfile;
import com.campost.backend.global.jwt.JwtTokenService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordHashService passwordHashService;
    private final JwtTokenService jwtTokenService;
    private final String dummyPasswordHash;

    public LoginService(
            UserRepository userRepository,
            PasswordHashService passwordHashService,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordHashService = passwordHashService;
        this.jwtTokenService = jwtTokenService;
        this.dummyPasswordHash = passwordHashService.hash("dummy-password");
    }

    public LoginResponse login(LoginRequest request) {
        Optional<User> foundUser = userRepository.findByUsername(request.username());
        String passwordHash = foundUser
                .map(User::passwordHash)
                .orElse(dummyPasswordHash);

        if (!passwordHashService.matches(request.password(), passwordHash) || foundUser.isEmpty()) {
            throw new BadCredentialsException();
        }

        User user = foundUser.get();
        String accessToken = jwtTokenService.generateAccessToken(
                user.id(),
                user.username(),
                user.name(),
                user.role()
        );
        String refreshToken = jwtTokenService.generateRefreshToken(user.id());

        // 온보딩 완료 여부는 서버 DB(users.profile_completed)를 단일 기준으로 삼는다.
        boolean profileCompleted = userRepository.findProfileById(user.id())
                .map(UserProfile::profileCompleted)
                .orElse(false);

        return LoginResponse.of(
                user.id(),
                user.username(),
                user.name(),
                user.role(),
                accessToken,
                refreshToken,
                jwtTokenService.accessTokenExpiryMs() / 1000,
                jwtTokenService.refreshTokenExpiryMs() / 1000,
                profileCompleted
        );
    }
}
