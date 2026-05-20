package com.campost.backend.domain.auth.repository;

import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.user.model.UserOnboardingProfile;
import com.campost.backend.domain.user.model.UserOnboardingProfileUpdateCommand;
import com.campost.backend.domain.user.model.UserProfile;
import com.campost.backend.domain.user.model.UserProfileUpdateCommand;

import java.util.Optional;

public interface UserRepository {

    User save(SignupUserCreateCommand command);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<UserProfile> findProfileById(long userId);

    Optional<UserProfile> updateProfile(UserProfileUpdateCommand command);

    Optional<UserOnboardingProfile> updateOnboardingProfile(UserOnboardingProfileUpdateCommand command);
}
