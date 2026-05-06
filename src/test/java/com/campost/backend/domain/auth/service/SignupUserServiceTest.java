package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.SignupRequest;
import com.campost.backend.domain.auth.exception.DuplicatedEmailException;
import com.campost.backend.domain.auth.exception.DuplicatedUsernameException;
import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void saveUserThrowsExceptionWhenEmailAlreadyExists() {
        SignupRequest request = new SignupRequest(
                "campost123",
                "campost@example.com",
                "password123"
        );
        userRepository.emailExists = true;

        assertThatThrownBy(() -> signupUserService.saveUser(request))
                .isInstanceOf(DuplicatedEmailException.class)
                .hasMessage("이미 가입된 이메일입니다.");

        assertThat(userRepository.savedCommand).isNull();
    }

    @Test
    void saveUserThrowsExceptionWhenUsernameAlreadyExists() {
        SignupRequest request = new SignupRequest(
                "campost123",
                "campost@example.com",
                "password123"
        );
        userRepository.usernameExists = true;

        assertThatThrownBy(() -> signupUserService.saveUser(request))
                .isInstanceOf(DuplicatedUsernameException.class)
                .hasMessage("이미 사용 중인 아이디입니다.");

        assertThat(userRepository.savedCommand).isNull();
        assertThat(userRepository.checkedEmail).isNull();
    }

    @Test
    void saveUserStoresUserWhenEmailDoesNotExist() {
        SignupRequest request = new SignupRequest(
                "campost123",
                "campost@example.com",
                "password123"
        );
        userRepository.emailExists = false;

        signupUserService.saveUser(request);

        assertThat(userRepository.savedCommand).isNotNull();
        assertThat(userRepository.savedCommand.email()).isEqualTo(request.email());
    }

    @Test
    void saveUserNormalizesEmailBeforeDuplicateCheckAndSave() {
        SignupRequest request = new SignupRequest(
                "campost123",
                " User@example.com ",
                "password123"
        );

        signupUserService.saveUser(request);

        assertThat(userRepository.checkedEmail).isEqualTo("user@example.com");
        assertThat(userRepository.savedCommand.email()).isEqualTo("user@example.com");
    }

    @Test
    void saveUserTrimsUsernameBeforeDuplicateCheckAndSave() {
        SignupRequest request = new SignupRequest(
                " campost123 ",
                "campost@example.com",
                "password123"
        );

        signupUserService.saveUser(request);

        assertThat(userRepository.checkedUsername).isEqualTo("campost123");
        assertThat(userRepository.savedCommand.username()).isEqualTo("campost123");
    }

    @Test
    void isUsernameAvailableReturnsFalseWhenUsernameExists() {
        userRepository.usernameExists = true;

        boolean available = signupUserService.isUsernameAvailable(" campost123 ");

        assertThat(available).isFalse();
        assertThat(userRepository.checkedUsername).isEqualTo("campost123");
    }

    @Test
    void isUsernameAvailableReturnsTrueWhenUsernameDoesNotExist() {
        userRepository.usernameExists = false;

        boolean available = signupUserService.isUsernameAvailable("campost123");

        assertThat(available).isTrue();
        assertThat(userRepository.checkedUsername).isEqualTo("campost123");
    }

    private static class FakeUserRepository implements UserRepository {

        private SignupUserCreateCommand savedCommand;
        private String checkedEmail;
        private String checkedUsername;
        private boolean emailExists;
        private boolean usernameExists;

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

        @Override
        public boolean existsByEmail(String email) {
            this.checkedEmail = email;
            return emailExists;
        }

        @Override
        public boolean existsByUsername(String username) {
            this.checkedUsername = username;
            return usernameExists;
        }
    }
}
