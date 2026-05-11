package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.EmailVerificationCodeRequest;
import com.campost.backend.domain.auth.dto.EmailVerificationCodeResponse;
import com.campost.backend.domain.auth.exception.DuplicatedEmailException;
import com.campost.backend.domain.auth.model.EmailVerificationCodeCreateCommand;
import com.campost.backend.domain.auth.repository.EmailVerificationRepository;
import com.campost.backend.domain.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;

@Service
public class EmailVerificationService {

    private static final long CODE_TTL_MINUTES = 5;

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordHashService passwordHashService;
    private final VerificationCodeGenerator verificationCodeGenerator;
    private final EmailVerificationSender emailVerificationSender;
    private final Clock clock;

    public EmailVerificationService(
            UserRepository userRepository,
            EmailVerificationRepository emailVerificationRepository,
            PasswordHashService passwordHashService,
            VerificationCodeGenerator verificationCodeGenerator,
            EmailVerificationSender emailVerificationSender
    ) {
        this(
                userRepository,
                emailVerificationRepository,
                passwordHashService,
                verificationCodeGenerator,
                emailVerificationSender,
                Clock.systemDefaultZone()
        );
    }

    EmailVerificationService(
            UserRepository userRepository,
            EmailVerificationRepository emailVerificationRepository,
            PasswordHashService passwordHashService,
            VerificationCodeGenerator verificationCodeGenerator,
            EmailVerificationSender emailVerificationSender,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.passwordHashService = passwordHashService;
        this.verificationCodeGenerator = verificationCodeGenerator;
        this.emailVerificationSender = emailVerificationSender;
        this.clock = clock;
    }

    @Transactional
    public EmailVerificationCodeResponse sendCode(EmailVerificationCodeRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicatedEmailException();
        }

        String code = verificationCodeGenerator.generate();
        String codeHash = passwordHashService.hash(code);
        OffsetDateTime expiresAt = OffsetDateTime.now(clock).plusMinutes(CODE_TTL_MINUTES);

        emailVerificationRepository.saveCode(new EmailVerificationCodeCreateCommand(
                normalizedEmail,
                codeHash,
                expiresAt
        ));
        emailVerificationSender.send(normalizedEmail, code);

        return new EmailVerificationCodeResponse(normalizedEmail, expiresAt);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
