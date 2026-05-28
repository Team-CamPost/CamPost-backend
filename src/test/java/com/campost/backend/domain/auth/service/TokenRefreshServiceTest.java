package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.TokenRefreshRequest;
import com.campost.backend.domain.auth.dto.TokenRefreshResponse;
import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.exception.UserNotFoundException;
import com.campost.backend.domain.user.model.UserOnboardingProfile;
import com.campost.backend.domain.user.model.UserOnboardingProfileUpdateCommand;
import com.campost.backend.domain.user.model.UserProfile;
import com.campost.backend.domain.user.model.UserProfileUpdateCommand;
import com.campost.backend.global.exception.InvalidTokenException;
import com.campost.backend.global.jwt.JwtTokenService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenRefreshServiceTest {

    private static final String JWT_SECRET = "01234567890123456789012345678901";
    private static final long ACCESS_TOKEN_EXPIRY_MS = 3_600_000L;
    private static final long REFRESH_TOKEN_EXPIRY_MS = 1_209_600_000L;

    private final FakeUserRepository userRepository = new FakeUserRepository();
    private final JwtTokenService jwtTokenService = new JwtTokenService(
            JWT_SECRET,
            ACCESS_TOKEN_EXPIRY_MS,
            REFRESH_TOKEN_EXPIRY_MS
    );
    private final TokenRefreshService tokenRefreshService = new TokenRefreshService(
            userRepository,
            jwtTokenService
    );

    @Test
    void refreshReturnsNewAccessTokenWithLatestUserInformation() {
        userRepository.user = Optional.of(new User(
                1L,
                "campost123",
                "Updated User",
                "campost@example.com",
                "password-hash",
                "ADMIN",
                OffsetDateTime.now()
        ));
        String refreshToken = jwtTokenService.generateRefreshToken(1L);

        TokenRefreshResponse response = tokenRefreshService.refresh(new TokenRefreshRequest(refreshToken));

        Claims accessClaims = jwtTokenService.parse(response.accessToken());
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(ACCESS_TOKEN_EXPIRY_MS / 1000);
        assertThat(accessClaims.getSubject()).isEqualTo("1");
        assertThat(accessClaims.get("username", String.class)).isEqualTo("campost123");
        assertThat(accessClaims.get("name", String.class)).isEqualTo("Updated User");
        assertThat(accessClaims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(accessClaims.get("tokenType", String.class)).isEqualTo(JwtTokenService.ACCESS_TOKEN_TYPE);
    }

    @Test
    void refreshRejectsAccessToken() {
        String accessToken = jwtTokenService.generateAccessToken(
                1L,
                "campost123",
                "CamPost User",
                "USER"
        );

        assertThatThrownBy(() -> tokenRefreshService.refresh(new TokenRefreshRequest(accessToken)))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshThrowsExceptionWhenUserDoesNotExist() {
        userRepository.user = Optional.empty();
        String refreshToken = jwtTokenService.generateRefreshToken(999L);

        assertThatThrownBy(() -> tokenRefreshService.refresh(new TokenRefreshRequest(refreshToken)))
                .isInstanceOf(UserNotFoundException.class);
    }

    private static class FakeUserRepository implements UserRepository {

        private Optional<User> user = Optional.empty();

        @Override
        public User save(SignupUserCreateCommand command) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public boolean existsByEmail(String email) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public boolean existsByUsername(String username) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public Optional<User> findByUsername(String username) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public Optional<User> findById(long userId) {
            return user;
        }

        @Override
        public Optional<UserProfile> findProfileById(long userId) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public Optional<UserProfile> updateProfile(UserProfileUpdateCommand command) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public boolean updatePasswordHash(long userId, String passwordHash) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public boolean deleteById(long userId) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public Optional<UserOnboardingProfile> updateOnboardingProfile(UserOnboardingProfileUpdateCommand command) {
            throw new UnsupportedOperationException("Not used in this test.");
        }
    }
}
