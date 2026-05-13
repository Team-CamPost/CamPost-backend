package com.campost.backend.domain.auth.repository;

import com.campost.backend.domain.auth.model.EmailVerificationCode;
import com.campost.backend.domain.auth.model.EmailVerificationCodeCreateCommand;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface EmailVerificationRepository {

    void saveCode(EmailVerificationCodeCreateCommand command);

    Optional<EmailVerificationCode> findByEmail(String email);

    void markVerified(String email, OffsetDateTime verifiedAt);

    boolean existsVerifiedEmail(String email);
}
