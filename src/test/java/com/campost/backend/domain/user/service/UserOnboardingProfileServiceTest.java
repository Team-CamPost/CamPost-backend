package com.campost.backend.domain.user.service;

import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.dto.OnboardingProfileRequest;
import com.campost.backend.domain.user.exception.UserNotFoundException;
import com.campost.backend.domain.user.model.UserOnboardingProfile;
import com.campost.backend.domain.user.model.UserOnboardingProfileUpdateCommand;
import com.campost.backend.domain.user.model.UserProfile;
import com.campost.backend.domain.user.model.UserProfileUpdateCommand;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserOnboardingProfileServiceTest {

    private final FakeUserRepository userRepository = new FakeUserRepository();
    private final UserOnboardingProfileService service = new UserOnboardingProfileService(userRepository);

    @Test
    void saveProfileTrimsNicknameAndMarksProfileCompleted() {
        OnboardingProfileRequest request = new OnboardingProfileRequest(
                "SW",
                3,
                " 캠포스트유저 "
        );

        UserOnboardingProfile profile = service.saveProfile(1L, request);

        assertThat(userRepository.savedCommand.userId()).isEqualTo(1L);
        assertThat(userRepository.savedCommand.department()).isEqualTo("SW");
        assertThat(userRepository.savedCommand.grade()).isEqualTo(3);
        assertThat(userRepository.savedCommand.nickname()).isEqualTo("캠포스트유저");
        assertThat(profile.nickname()).isEqualTo("캠포스트유저");
        assertThat(profile.profileCompleted()).isTrue();
    }

    @Test
    void saveProfileThrowsExceptionWhenUserDoesNotExist() {
        userRepository.updatedProfile = Optional.empty();
        OnboardingProfileRequest request = new OnboardingProfileRequest("SW", 2, "캠포스트유저");

        assertThatThrownBy(() -> service.saveProfile(999L, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    private static class FakeUserRepository implements UserRepository {

        private UserOnboardingProfileUpdateCommand savedCommand;
        private Optional<UserOnboardingProfile> updatedProfile;

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
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public boolean updatePasswordHash(long userId, String passwordHash) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public Optional<UserOnboardingProfile> updateOnboardingProfile(UserOnboardingProfileUpdateCommand command) {
            this.savedCommand = command;
            if (updatedProfile != null) {
                return updatedProfile;
            }

            return Optional.of(new UserOnboardingProfile(
                    command.userId(),
                    command.department(),
                    command.grade(),
                    command.nickname(),
                    true
            ));
        }
    }
}
