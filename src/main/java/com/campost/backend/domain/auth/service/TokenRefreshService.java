package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.TokenRefreshRequest;
import com.campost.backend.domain.auth.dto.TokenRefreshResponse;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.exception.UserNotFoundException;
import com.campost.backend.global.exception.InvalidTokenException;
import com.campost.backend.global.jwt.JwtTokenService;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenRefreshService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;

    public TokenRefreshService(
            UserRepository userRepository,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        Claims claims = jwtTokenService.parse(request.refreshToken());

        if (!JwtTokenService.REFRESH_TOKEN_TYPE.equals(claims.get("tokenType", String.class))) {
            throw new InvalidTokenException("Invalid refresh token.");
        }

        long userId = parseUserId(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        String accessToken = jwtTokenService.generateAccessToken(
                user.id(),
                user.username(),
                user.name(),
                user.role()
        );

        return TokenRefreshResponse.of(accessToken, jwtTokenService.accessTokenExpiryMs() / 1000);
    }

    private long parseUserId(String subject) {
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            throw new InvalidTokenException("Invalid token subject.");
        }
    }
}
