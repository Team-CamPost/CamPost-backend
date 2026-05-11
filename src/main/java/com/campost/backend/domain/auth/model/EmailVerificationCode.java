package com.campost.backend.domain.auth.model;

import java.time.OffsetDateTime;

public record EmailVerificationCode(
        String email,
        String codeHash,
        OffsetDateTime expiresAt,
        OffsetDateTime verifiedAt
) {
}
