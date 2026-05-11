-- ============================================================
-- Email verification codes for signup
-- ============================================================

CREATE TABLE IF NOT EXISTS email_verification_codes (
    id              BIGSERIAL       PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    code_hash       VARCHAR(255)    NOT NULL,
    expires_at      TIMESTAMPTZ     NOT NULL,
    verified_at     TIMESTAMPTZ,
    issued_at       TIMESTAMPTZ     NOT NULL DEFAULT now()
);
