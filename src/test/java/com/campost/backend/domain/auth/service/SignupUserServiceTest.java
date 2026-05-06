package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.SignupRequest;
import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SignupUserServiceTest {

    private final FakeUserRepository userRepository = new FakeUserRepository();
    private final PasswordHashService passwordHashService = new PasswordHashService();
    private final SignupUserService signupUserService = new SignupUserService(
            userRepository,
            passwordHashService
    );

    @Test
    void saveUserHashesPasswordBeforeSaving() {
        SignupRequest request = new SignupRequest(
                "campost123",
                "campost@example.com",
                "password123"
        );

        User savedUser = signupUserService.saveUser(request);
        SignupUserCreateCommand savedCommand = userRepository.savedCommand;

        assertThat(savedCommand.username()).isEqualTo(request.username());
        assertThat(savedCommand.email()).isEqualTo(request.email());
        assertThat(savedCommand.passwordHash()).isNotEqualTo(request.password());
        assertThat(savedCommand.passwordHash()).doesNotContain(request.password());
        assertThat(passwordHashService.matches(request.password(), savedCommand.passwordHash())).isTrue();
        assertThat(savedUser.passwordHash()).isEqualTo(savedCommand.passwordHash());
    }

    private static class FakeUserRepository implements UserRepository {

        private SignupUserCreateCommand savedCommand;

        @Override
        public User save(SignupUserCreateCommand command) {
            this.savedCommand = command;

            return new User(
                    1L,
                    command.username(),
                    command.email(),
                    command.passwordHash(),
                    "GUEST",
                    OffsetDateTime.now()
            );
        }
    }
}
