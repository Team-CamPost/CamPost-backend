package com.campost.backend.domain.auth.service;

import com.campost.backend.domain.auth.exception.InvalidEmailVerificationCodeException;
import com.campost.backend.domain.auth.model.EmailVerificationCode;
import com.campost.backend.domain.auth.repository.EmailVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class EmailVerificationCodeVerifyService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final VerificationCodeHashService verificationCodeHashService;
    private final Clock clock;

    public EmailVerificationCodeVerifyService(
            EmailVerificationRepository emailVerificationRepository,
            VerificationCodeHashService verificationCodeHashService,
            Clock clock
    ) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.verificationCodeHashService = verificationCodeHashService;
        this.clock = clock;
    }

    @Transactional
    public OffsetDateTime verify(String email, String code) {
        EmailVerificationCode verificationCode = emailVerificationRepository.findByEmail(email)
                .orElseThrow(InvalidEmailVerificationCodeException::new);
        OffsetDateTime now = OffsetDateTime.now(clock);

        if (!verificationCode.expiresAt().isAfter(now)) {
            throw new InvalidEmailVerificationCodeException();
        }

        if (!verificationCodeHashService.matches(code, verificationCode.codeHash())) {
            throw new InvalidEmailVerificationCodeException();
        }

        emailVerificationRepository.markVerified(email, now);
        return now;
    }
}
