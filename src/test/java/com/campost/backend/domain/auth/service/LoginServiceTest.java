package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.LoginRequest;
import com.campost.backend.domain.auth.exception.BadCredentialsException;
import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.model.UserOnboardingProfile;
import com.campost.backend.domain.user.model.UserOnboardingProfileUpdateCommand;
import com.campost.backend.domain.user.model.UserProfile;
import com.campost.backend.domain.user.model.UserProfileUpdateCommand;
import com.campost.backend.global.jwt.JwtTokenService;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginServiceTest {

    private static final String JWT_SECRET = "01234567890123456789012345678901";

    private final FakeUserRepository userRepository = new FakeUserRepository();
    private final CountingPasswordHashService passwordHashService = new CountingPasswordHashService();
    private final LoginService loginService = new LoginService(
            userRepository,
            passwordHashService,
            new JwtTokenService(JWT_SECRET, 3_600_000L)
    );

    @Test
    void loginUsesDummyPasswordHashWhenUserDoesNotExist() {
        userRepository.foundUser = Optional.empty();

        assertThatThrownBy(() -> loginService.login(new LoginRequest("missing-user", "password123")))
                .isInstanceOf(BadCredentialsException.class);

        assertThat(passwordHashService.matchedPassword).isEqualTo("password123");
        assertThat(passwordHashService.matchedHash).isEqualTo("dummy-hash");
        assertThat(passwordHashService.matchesCount).isEqualTo(1);
    }

    @Test
    void loginStillRejectsExistingUserWhenPasswordDoesNotMatch() {
        userRepository.foundUser = Optional.of(new User(
                1L,
                "campost123",
                "CamPost User",
                "campost@example.com",
                "real-hash",
                "GUEST",
                OffsetDateTime.now()
        ));
        passwordHashService.matchesResult = false;

        assertThatThrownBy(() -> loginService.login(new LoginRequest("campost123", "wrong-password")))
                .isInstanceOf(BadCredentialsException.class);

        assertThat(passwordHashService.matchedHash).isEqualTo("real-hash");
        assertThat(passwordHashService.matchesCount).isEqualTo(1);
    }

    private static class FakeUserRepository implements UserRepository {

        private Optional<User> foundUser = Optional.empty();

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
            return foundUser;
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
        public Optional<UserOnboardingProfile> updateOnboardingProfile(UserOnboardingProfileUpdateCommand command) {
            throw new UnsupportedOperationException("Not used in this test.");
        }
    }

    private static class CountingPasswordHashService extends PasswordHashService {

        private String matchedPassword;
        private String matchedHash;
        private int matchesCount;
        private boolean matchesResult = true;

        @Override
        public String hash(String rawPassword) {
            return "dummy-hash";
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            this.matchedPassword = rawPassword;
            this.matchedHash = encodedPassword;
            this.matchesCount++;
            return matchesResult;
        }
    }
}
