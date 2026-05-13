-- ============================================================
-- Use an explicit unique index name for users.email
-- ============================================================

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_email_key;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users(email);
