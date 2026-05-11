package com.campost.backend.domain.auth.repository;

import com.campost.backend.domain.auth.model.EmailVerificationCodeCreateCommand;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

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
}
