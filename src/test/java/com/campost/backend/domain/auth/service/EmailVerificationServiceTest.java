package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.EmailVerificationCodeRequest;
import com.campost.backend.domain.auth.dto.EmailVerificationCodeResponse;
import com.campost.backend.domain.auth.dto.EmailVerificationCheckRequest;
import com.campost.backend.domain.auth.dto.EmailVerificationCheckResponse;
import com.campost.backend.domain.auth.exception.DuplicatedEmailException;
import com.campost.backend.domain.auth.exception.InvalidEmailVerificationCodeException;
import com.campost.backend.domain.auth.model.EmailVerificationCode;
import com.campost.backend.domain.auth.model.EmailVerificationCodeCreateCommand;
import com.campost.backend.domain.auth.model.SignupUserCreateCommand;
import com.campost.backend.domain.auth.model.User;
import com.campost.backend.domain.auth.repository.EmailVerificationRepository;
import com.campost.backend.domain.auth.repository.UserRepository;
import com.campost.backend.domain.user.model.UserOnboardingProfile;
import com.campost.backend.domain.user.model.UserOnboardingProfileUpdateCommand;
import com.campost.backend.domain.user.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailVerificationServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-07T03:00:00Z"),
            ZoneId.of("UTC")
    );

    private final FakeUserRepository userRepository = new FakeUserRepository();
    private final FakeEmailVerificationRepository emailVerificationRepository =
            new FakeEmailVerificationRepository();
    private final EmailVerificationCodeIssueService emailVerificationCodeIssueService =
            new EmailVerificationCodeIssueService(userRepository, emailVerificationRepository);
    private final VerificationCodeHashService verificationCodeHashService =
            new VerificationCodeHashService();
    private final EmailVerificationCodeVerifyService emailVerificationCodeVerifyService =
            new EmailVerificationCodeVerifyService(
                    emailVerificationRepository,
                    verificationCodeHashService,
                    FIXED_CLOCK
            );
    private final FakeVerificationCodeGenerator verificationCodeGenerator =
            new FakeVerificationCodeGenerator();
    private final FakeEmailVerificationSender emailVerificationSender =
            new FakeEmailVerificationSender();
    private final EmailVerificationService emailVerificationService = new EmailVerificationService(
            emailVerificationCodeIssueService,
            emailVerificationCodeVerifyService,
            verificationCodeHashService,
            verificationCodeGenerator,
            emailVerificationSender,
            FIXED_CLOCK
    );

    @Test
    void sendCodeSavesHashedCodeAndSendsRawCode() {
        EmailVerificationCodeRequest request = new EmailVerificationCodeRequest(" User@example.com ");

        EmailVerificationCodeResponse response = emailVerificationService.sendCode(request);
        EmailVerificationCodeCreateCommand savedCommand = emailVerificationRepository.savedCommand;

        assertThat(userRepository.checkedEmail).isEqualTo("user@example.com");
        assertThat(savedCommand.email()).isEqualTo("user@example.com");
        assertThat(savedCommand.codeHash()).isNotEqualTo("123456");
        assertThat(savedCommand.codeHash()).doesNotContain("123456");
        assertThat(verificationCodeHashService.matches("123456", savedCommand.codeHash())).isTrue();
        assertThat(savedCommand.expiresAt()).isEqualTo(OffsetDateTime.now(FIXED_CLOCK).plusMinutes(5));
        assertThat(emailVerificationSender.sentEmail).isEqualTo("user@example.com");
        assertThat(emailVerificationSender.sentCode).isEqualTo("123456");
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.expiresAt()).isEqualTo(savedCommand.expiresAt());
    }

    @Test
    void sendCodeThrowsExceptionWhenEmailAlreadyExists() {
        EmailVerificationCodeRequest request = new EmailVerificationCodeRequest("campost@example.com");
        userRepository.emailExists = true;

        assertThatThrownBy(() -> emailVerificationService.sendCode(request))
                .isInstanceOf(DuplicatedEmailException.class)
                .hasMessage("이미 가입된 이메일입니다.");

        assertThat(emailVerificationRepository.savedCommand).isNull();
        assertThat(emailVerificationSender.sentEmail).isNull();
    }

    @Test
    void verifyCodeMarksEmailVerified() {
        EmailVerificationCheckRequest request = new EmailVerificationCheckRequest(
                " User@example.com ",
                "123456"
        );
        emailVerificationRepository.savedVerificationCode = new EmailVerificationCode(
                "user@example.com",
                verificationCodeHashService.hash("123456"),
                OffsetDateTime.now(FIXED_CLOCK).plusMinutes(5),
                null
        );

        EmailVerificationCheckResponse response = emailVerificationService.verifyCode(request);

        assertThat(emailVerificationRepository.checkedEmail).isEqualTo("user@example.com");
        assertThat(emailVerificationRepository.verifiedEmail).isEqualTo("user@example.com");
        assertThat(emailVerificationRepository.verifiedAt).isEqualTo(OffsetDateTime.now(FIXED_CLOCK));
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.verified()).isTrue();
        assertThat(response.verifiedAt()).isEqualTo(OffsetDateTime.now(FIXED_CLOCK));
    }

    @Test
    void verifyCodeThrowsExceptionWhenCodeDoesNotMatch() {
        EmailVerificationCheckRequest request = new EmailVerificationCheckRequest(
                "campost@example.com",
                "000000"
        );
        emailVerificationRepository.savedVerificationCode = new EmailVerificationCode(
                "campost@example.com",
                verificationCodeHashService.hash("123456"),
                OffsetDateTime.now(FIXED_CLOCK).plusMinutes(5),
                null
        );

        assertThatThrownBy(() -> emailVerificationService.verifyCode(request))
                .isInstanceOf(InvalidEmailVerificationCodeException.class);

        assertThat(emailVerificationRepository.verifiedEmail).isNull();
    }

    @Test
    void verifyCodeThrowsExceptionWhenCodeExpired() {
        EmailVerificationCheckRequest request = new EmailVerificationCheckRequest(
                "campost@example.com",
                "123456"
        );
        emailVerificationRepository.savedVerificationCode = new EmailVerificationCode(
                "campost@example.com",
                verificationCodeHashService.hash("123456"),
                OffsetDateTime.now(FIXED_CLOCK).minusSeconds(1),
                null
        );

        assertThatThrownBy(() -> emailVerificationService.verifyCode(request))
                .isInstanceOf(InvalidEmailVerificationCodeException.class);

        assertThat(emailVerificationRepository.verifiedEmail).isNull();
    }

    @Test
    void randomVerificationCodeGeneratorCreatesSixDigitCode() {
        RandomVerificationCodeGenerator generator = new RandomVerificationCodeGenerator();

        assertThat(generator.generate()).matches("\\d{6}");
    }

    private static class FakeUserRepository implements UserRepository {

        private String checkedEmail;
        private boolean emailExists;

        @Override
        public User save(SignupUserCreateCommand command) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public boolean existsByEmail(String email) {
            this.checkedEmail = email;
            return emailExists;
        }

        @Override
        public boolean existsByUsername(String username) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public java.util.Optional<User> findByUsername(String username) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public Optional<UserProfile> findProfileById(long userId) {
            throw new UnsupportedOperationException("Not used in this test.");
        }

        @Override
        public Optional<UserOnboardingProfile> updateOnboardingProfile(UserOnboardingProfileUpdateCommand command) {
            throw new UnsupportedOperationException("Not used in this test.");
        }
    }

    private static class FakeEmailVerificationRepository implements EmailVerificationRepository {

        private EmailVerificationCodeCreateCommand savedCommand;
        private EmailVerificationCode savedVerificationCode;
        private String checkedEmail;
        private String verifiedEmail;
        private OffsetDateTime verifiedAt;

        @Override
        public void saveCode(EmailVerificationCodeCreateCommand command) {
            this.savedCommand = command;
        }

        @Override
        public Optional<EmailVerificationCode> findByEmail(String email) {
            this.checkedEmail = email;
            return Optional.ofNullable(savedVerificationCode);
        }

        @Override
        public void markVerified(String email, OffsetDateTime verifiedAt) {
            this.verifiedEmail = email;
            this.verifiedAt = verifiedAt;
        }

        @Override
        public boolean existsVerifiedEmail(String email) {
            throw new UnsupportedOperationException("Not used in this test.");
        }
    }

    private static class FakeVerificationCodeGenerator implements VerificationCodeGenerator {

        @Override
        public String generate() {
            return "123456";
        }
    }

    private static class FakeEmailVerificationSender implements EmailVerificationSender {

        private String sentEmail;
        private String sentCode;

        @Override
        public void send(String email, String code) {
            this.sentEmail = email;
            this.sentCode = code;
        }
    }
}
