-- ============================================================
-- Add username for signup
-- ============================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS username VARCHAR(20);

UPDATE users
SET username = CASE
    WHEN email = 'admin@dankook.ac.kr' THEN 'admin01'
    ELSE 'user' || LPAD(id::TEXT, 6, '0')
END
WHERE username IS NULL;

ALTER TABLE users
    ALTER COLUMN username SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_username ON users(username);
