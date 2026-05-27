package com.campost.backend.domain.user.service;

import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.dto.UserProfileUpdateRequest;
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

class UserProfileUpdateServiceTest {

    private final FakeUserRepository userRepository = new FakeUserRepository();
    private final UserProfileUpdateService service = new UserProfileUpdateService(userRepository);

    @Test
    void updateProfileTrimsNicknameAndReturnsUpdatedProfile() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("SW", 3, " 캠포스트유저 ");

        UserProfile profile = service.updateProfile(1L, request);

        assertThat(userRepository.savedCommand.userId()).isEqualTo(1L);
        assertThat(userRepository.savedCommand.department()).isEqualTo("SW");
        assertThat(userRepository.savedCommand.grade()).isEqualTo(3);
        assertThat(userRepository.savedCommand.nickname()).isEqualTo("캠포스트유저");
        assertThat(profile.nickname()).isEqualTo("캠포스트유저");
        assertThat(profile.department()).isEqualTo("SW");
        assertThat(profile.grade()).isEqualTo(3);
        assertThat(profile.profileCompleted()).isTrue();
    }

    @Test
    void updateProfileThrowsExceptionWhenUserDoesNotExist() {
        userRepository.updatedProfile = Optional.empty();
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("SW", 2, "캠포스트유저");

        assertThatThrownBy(() -> service.updateProfile(999L, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    private static class FakeUserRepository implements UserRepository {

        private UserProfileUpdateCommand savedCommand;
        private Optional<UserProfile> updatedProfile;

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
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public Optional<UserProfile> findProfileById(long userId) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public Optional<UserProfile> updateProfile(UserProfileUpdateCommand command) {
            this.savedCommand = command;
            if (updatedProfile != null) {
                return updatedProfile;
            }

            return Optional.of(new UserProfile(
                    command.userId(),
                    "campost123",
                    "campost@example.com",
                    command.nickname(),
                    command.department(),
                    command.grade(),
                    "GUEST",
                    true,
                    OffsetDateTime.parse("2026-05-20T01:00:00Z"),
                    OffsetDateTime.parse("2026-05-20T02:00:00Z")
            ));
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
