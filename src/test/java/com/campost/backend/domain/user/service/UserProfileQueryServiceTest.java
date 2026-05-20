package com.campost.backend.domain.user.service;

import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.exception.UserNotFoundException;
import com.campost.backend.domain.user.model.UserOnboardingProfile;
import com.campost.backend.domain.user.model.UserOnboardingProfileUpdateCommand;
import com.campost.backend.domain.user.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProfileQueryServiceTest {

    private final FakeUserRepository userRepository = new FakeUserRepository();
    private final UserProfileQueryService service = new UserProfileQueryService(userRepository);

    @Test
    void getProfileReturnsCurrentUserProfile() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-05-20T01:00:00Z");
        OffsetDateTime lastLoginAt = OffsetDateTime.parse("2026-05-20T02:00:00Z");
        userRepository.profile = Optional.of(new UserProfile(
                1L,
                "campost123",
                "campost@example.com",
                "캠포스트유저",
                "SW",
                3,
                "GUEST",
                true,
                createdAt,
                lastLoginAt
        ));

        UserProfile profile = service.getProfile(1L);

        assertThat(userRepository.checkedUserId).isEqualTo(1L);
        assertThat(profile.username()).isEqualTo("campost123");
        assertThat(profile.email()).isEqualTo("campost@example.com");
        assertThat(profile.nickname()).isEqualTo("캠포스트유저");
        assertThat(profile.department()).isEqualTo("SW");
        assertThat(profile.grade()).isEqualTo(3);
        assertThat(profile.profileCompleted()).isTrue();
        assertThat(profile.createdAt()).isEqualTo(createdAt);
        assertThat(profile.lastLoginAt()).isEqualTo(lastLoginAt);
    }

    @Test
    void getProfileThrowsExceptionWhenUserDoesNotExist() {
        userRepository.profile = Optional.empty();

        assertThatThrownBy(() -> service.getProfile(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    private static class FakeUserRepository implements UserRepository {

        private long checkedUserId;
        private Optional<UserProfile> profile = Optional.empty();

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
        public Optional<UserProfile> findProfileById(long userId) {
            this.checkedUserId = userId;
            return profile;
        }

        @Override
        public Optional<UserOnboardingProfile> updateOnboardingProfile(UserOnboardingProfileUpdateCommand command) {
            throw new UnsupportedOperationException("Not used in this test.");
        }
    }
}
