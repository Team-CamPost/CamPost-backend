package com.campost.backend.domain.auth.repository;

import com.campost.backend.domain.auth.model.EmailVerificationCodeCreateCommand;

public interface EmailVerificationRepository {

    void saveCode(EmailVerificationCodeCreateCommand command);
}
