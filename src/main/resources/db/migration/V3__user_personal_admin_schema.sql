-- ============================================================
-- CamPost User/Personal/Admin Schema
-- - users
-- - bookmarks
-- - keywords
-- - notifications
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL       PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    name            VARCHAR(50),
    department      VARCHAR(100),
    grade           SMALLINT        CHECK (grade BETWEEN 1 AND 6),
    role            VARCHAR(20)     NOT NULL DEFAULT 'GUEST'
                                    CHECK (role IN ('GUEST','USER','ADMIN')),
    created_at      TIMESTAMPTZ     DEFAULT now(),
    last_login_at   TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS bookmarks (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    article_id      VARCHAR(30)     NOT NULL,
    created_at      TIMESTAMPTZ     DEFAULT now(),
    UNIQUE (user_id, article_id)
);

CREATE INDEX IF NOT EXISTS idx_bookmarks_user_id ON bookmarks(user_id);

CREATE TABLE IF NOT EXISTS keywords (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    keyword         VARCHAR(50)     NOT NULL,
    created_at      TIMESTAMPTZ     DEFAULT now(),
    UNIQUE (user_id, keyword)
);

CREATE INDEX IF NOT EXISTS idx_keywords_user_id ON keywords(user_id);

CREATE TABLE IF NOT EXISTS notifications (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    article_id      VARCHAR(30),
    type            VARCHAR(30)     NOT NULL
                                    CHECK (type IN ('deadline_d1','deadline_d3','keyword_match','system')),
    message         TEXT            NOT NULL,
    is_read         BOOLEAN         DEFAULT false,
    created_at      TIMESTAMPTZ     DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread  ON notifications(user_id, is_read);
