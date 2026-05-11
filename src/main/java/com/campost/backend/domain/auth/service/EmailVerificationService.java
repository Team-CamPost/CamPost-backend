package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.dto.EmailVerificationCodeRequest;
import com.campost.backend.domain.auth.dto.EmailVerificationCodeResponse;
import com.campost.backend.domain.auth.model.EmailVerificationCodeCreateCommand;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;

@Service
public class EmailVerificationService {

    private static final long CODE_TTL_MINUTES = 5;

    private final EmailVerificationCodeIssueService emailVerificationCodeIssueService;
    private final VerificationCodeHashService verificationCodeHashService;
    private final VerificationCodeGenerator verificationCodeGenerator;
    private final EmailVerificationSender emailVerificationSender;
    private final Clock clock;

    public EmailVerificationService(
            EmailVerificationCodeIssueService emailVerificationCodeIssueService,
            VerificationCodeHashService verificationCodeHashService,
            VerificationCodeGenerator verificationCodeGenerator,
            EmailVerificationSender emailVerificationSender
    ) {
        this(
                emailVerificationCodeIssueService,
                verificationCodeHashService,
                verificationCodeGenerator,
                emailVerificationSender,
                Clock.systemDefaultZone()
        );
    }

    EmailVerificationService(
            EmailVerificationCodeIssueService emailVerificationCodeIssueService,
            VerificationCodeHashService verificationCodeHashService,
            VerificationCodeGenerator verificationCodeGenerator,
            EmailVerificationSender emailVerificationSender,
            Clock clock
    ) {
        this.emailVerificationCodeIssueService = emailVerificationCodeIssueService;
        this.verificationCodeHashService = verificationCodeHashService;
        this.verificationCodeGenerator = verificationCodeGenerator;
        this.emailVerificationSender = emailVerificationSender;
        this.clock = clock;
    }

    public EmailVerificationCodeResponse sendCode(EmailVerificationCodeRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String code = verificationCodeGenerator.generate();
        String codeHash = verificationCodeHashService.hash(code);
        OffsetDateTime expiresAt = OffsetDateTime.now(clock).plusMinutes(CODE_TTL_MINUTES);

        emailVerificationCodeIssueService.issueCode(new EmailVerificationCodeCreateCommand(
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
