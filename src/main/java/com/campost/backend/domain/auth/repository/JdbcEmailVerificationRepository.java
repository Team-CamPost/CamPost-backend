package com.campost.backend.domain.auth.repository;

import com.campost.backend.domain.auth.model.EmailVerificationCode;
import com.campost.backend.domain.auth.model.EmailVerificationCodeCreateCommand;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class JdbcEmailVerificationRepository implements EmailVerificationRepository {

    private final JdbcClient jdbcClient;

    public JdbcEmailVerificationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void saveCode(EmailVerificationCodeCreateCommand command) {
        String sql = """
                INSERT INTO email_verification_codes (email, code_hash, expires_at, verified_at, issued_at)
                VALUES (:email, :codeHash, :expiresAt, NULL, now())
                ON CONFLICT (email) DO UPDATE SET
                    code_hash = EXCLUDED.code_hash,
                    expires_at = EXCLUDED.expires_at,
                    verified_at = NULL,
                    issued_at = now()
                """;

        jdbcClient.sql(sql)
                .param("email", command.email())
                .param("codeHash", command.codeHash())
                .param("expiresAt", command.expiresAt())
                .update();
    }

    @Override
    public Optional<EmailVerificationCode> findByEmail(String email) {
        String sql = """
                SELECT email, code_hash, expires_at, verified_at
                FROM email_verification_codes
                WHERE email = :email
                """;

        return jdbcClient.sql(sql)
                .param("email", email)
                .query((rs, rowNum) -> new EmailVerificationCode(
                        rs.getString("email"),
                        rs.getString("code_hash"),
                        rs.getObject("expires_at", OffsetDateTime.class),
                        rs.getObject("verified_at", OffsetDateTime.class)
                ))
                .optional();
    }

    @Override
    public void markVerified(String email, OffsetDateTime verifiedAt) {
        String sql = """
                UPDATE email_verification_codes
                SET verified_at = :verifiedAt
                WHERE email = :email
                """;

        jdbcClient.sql(sql)
                .param("email", email)
                .param("verifiedAt", verifiedAt)
                .update();
    }

    @Override
    public boolean existsVerifiedEmail(String email) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM email_verification_codes
                    WHERE email = :email
                      AND verified_at IS NOT NULL
                )
                """;

        return Boolean.TRUE.equals(jdbcClient.sql(sql)
                .param("email", email)
                .query(Boolean.class)
                .single());
    }
}
