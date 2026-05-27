package com.campost.backend.domain.user.service;

import com.campost.backend.domain.auth.exception.BadCredentialsException;
import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.auth.service.PasswordHashService;
import com.campost.backend.domain.user.dto.UserAccountDeleteRequest;
import com.campost.backend.domain.user.exception.UserNotFoundException;
import com.campost.backend.domain.user.model.UserOnboardingProfile;
import com.campost.backend.domain.user.model.UserOnboardingProfileUpdateCommand;
import com.campost.backend.domain.user.model.UserProfile;
import com.campost.backend.domain.user.model.UserProfileUpdateCommand;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAccountDeleteServiceTest {

    private final FakeUserRepository userRepository = new FakeUserRepository();
    private final PasswordHashService passwordHashService = new PasswordHashService();
    private final UserAccountDeleteService service = new UserAccountDeleteService(
            userRepository,
            passwordHashService
    );

    @Test
    void deleteAccountVerifiesCurrentPasswordAndDeletesUser() {
        userRepository.user = Optional.of(userWithPasswordHash(passwordHashService.hash("password123")));

        service.deleteAccount(1L, new UserAccountDeleteRequest("password123"));

        assertThat(userRepository.checkedUserId).isEqualTo(1L);
        assertThat(userRepository.deletedUserId).isEqualTo(1L);
    }

    @Test
    void deleteAccountThrowsExceptionWhenCurrentPasswordDoesNotMatch() {
        userRepository.user = Optional.of(userWithPasswordHash(passwordHashService.hash("password123")));

        assertThatThrownBy(() -> service.deleteAccount(
                1L,
                new UserAccountDeleteRequest("wrongPassword123")
        )).isInstanceOf(BadCredentialsException.class);

        assertThat(userRepository.deletedUserId).isZero();
    }

    @Test
    void deleteAccountThrowsExceptionWhenUserDoesNotExist() {
        userRepository.user = Optional.empty();

        assertThatThrownBy(() -> service.deleteAccount(
                999L,
                new UserAccountDeleteRequest("password123")
        )).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteAccountThrowsExceptionWhenDeleteFails() {
        userRepository.user = Optional.of(userWithPasswordHash(passwordHashService.hash("password123")));
        userRepository.deleteResult = false;

        assertThatThrownBy(() -> service.deleteAccount(
                1L,
                new UserAccountDeleteRequest("password123")
        )).isInstanceOf(UserNotFoundException.class);
    }

    private User userWithPasswordHash(String passwordHash) {
        return new User(
                1L,
                "campost123",
                "CamPost User",
                "campost@example.com",
                passwordHash,
                "GUEST",
                OffsetDateTime.parse("2026-05-20T01:00:00Z")
        );
    }

    private static class FakeUserRepository implements UserRepository {

        private Optional<User> user = Optional.empty();
        private long checkedUserId;
        private long deletedUserId;
        private boolean deleteResult = true;

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
            this.checkedUserId = userId;
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
            this.deletedUserId = userId;
            return deleteResult;
        }

        @Override
        public Optional<UserOnboardingProfile> updateOnboardingProfile(UserOnboardingProfileUpdateCommand command) {
            throw new UnsupportedOperationException("Not used in this test.");
        }
    }
}
