package com.campost.backend.domain.auth.model;

import java.time.OffsetDateTime;

public record EmailVerificationCodeCreateCommand(
        String email,
        String codeHash,
        OffsetDateTime expiresAt
) {
}
