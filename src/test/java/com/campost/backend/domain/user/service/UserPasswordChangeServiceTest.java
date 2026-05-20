package com.campost.backend.domain.user.service;

import com.campost.backend.domain.auth.exception.BadCredentialsException;
import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.auth.service.PasswordHashService;
import com.campost.backend.domain.user.dto.UserPasswordChangeRequest;
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

class UserPasswordChangeServiceTest {

    private final FakeUserRepository userRepository = new FakeUserRepository();
    private final PasswordHashService passwordHashService = new PasswordHashService();
    private final UserPasswordChangeService service = new UserPasswordChangeService(
            userRepository,
            passwordHashService
    );

    @Test
    void changePasswordVerifiesCurrentPasswordAndStoresHashedNewPassword() {
        userRepository.user = Optional.of(userWithPasswordHash(passwordHashService.hash("password123")));

        service.changePassword(1L, new UserPasswordChangeRequest("password123", "newPassword123"));

        assertThat(userRepository.checkedUserId).isEqualTo(1L);
        assertThat(userRepository.updatedUserId).isEqualTo(1L);
        assertThat(userRepository.updatedPasswordHash).isNotEqualTo("newPassword123");
        assertThat(userRepository.updatedPasswordHash).doesNotContain("newPassword123");
        assertThat(passwordHashService.matches("newPassword123", userRepository.updatedPasswordHash)).isTrue();
    }

    @Test
    void changePasswordThrowsExceptionWhenCurrentPasswordDoesNotMatch() {
        userRepository.user = Optional.of(userWithPasswordHash(passwordHashService.hash("password123")));

        assertThatThrownBy(() -> service.changePassword(
                1L,
                new UserPasswordChangeRequest("wrongPassword123", "newPassword123")
        )).isInstanceOf(BadCredentialsException.class);

        assertThat(userRepository.updatedPasswordHash).isNull();
    }

    @Test
    void changePasswordThrowsExceptionWhenUserDoesNotExist() {
        userRepository.user = Optional.empty();

        assertThatThrownBy(() -> service.changePassword(
                999L,
                new UserPasswordChangeRequest("password123", "newPassword123")
        )).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void changePasswordThrowsExceptionWhenUpdateFails() {
        userRepository.user = Optional.of(userWithPasswordHash(passwordHashService.hash("password123")));
        userRepository.updateResult = false;

        assertThatThrownBy(() -> service.changePassword(
                1L,
                new UserPasswordChangeRequest("password123", "newPassword123")
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
        private long updatedUserId;
        private String updatedPasswordHash;
        private boolean updateResult = true;

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
            this.updatedUserId = userId;
            this.updatedPasswordHash = passwordHash;
            return updateResult;
        }

        @Override
        public Optional<UserOnboardingProfile> updateOnboardingProfile(UserOnboardingProfileUpdateCommand command) {
            throw new UnsupportedOperationException("Not used in this test.");
        }
    }
}
