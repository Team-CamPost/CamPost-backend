-- ============================================================
-- CamPost Post Domain Schema
-- - raw_notices
-- - notices
-- - notice_attachments
-- - raw_import_log
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE TABLE IF NOT EXISTS raw_notices (
    id              BIGSERIAL       PRIMARY KEY,
    source_id       BIGINT          REFERENCES crawl_sources(id),
    article_id      VARCHAR(30)     NOT NULL UNIQUE,
    title           TEXT            NOT NULL,
    is_pinned       BOOLEAN         DEFAULT false,
    post_number     VARCHAR(20),
    author          VARCHAR(100),
    date            VARCHAR(20),
    views           VARCHAR(20),
    has_attachment  BOOLEAN         DEFAULT false,
    category        VARCHAR(50),
    body_text       TEXT,
    source_url      TEXT,
    hash            CHAR(64)        NOT NULL,
    crawled_at      TIMESTAMPTZ     DEFAULT now(),
    parse_status    VARCHAR(20)     DEFAULT 'pending'
                                    CHECK (parse_status IN ('pending', 'done', 'failed'))
);

CREATE INDEX IF NOT EXISTS idx_raw_notices_source_id    ON raw_notices(source_id);
CREATE INDEX IF NOT EXISTS idx_raw_notices_parse_status ON raw_notices(parse_status);
CREATE INDEX IF NOT EXISTS idx_raw_notices_crawled_at   ON raw_notices(crawled_at DESC);

CREATE TABLE IF NOT EXISTS notices (
    id              BIGSERIAL       PRIMARY KEY,
    raw_notice_id   BIGINT          NOT NULL REFERENCES raw_notices(id) ON DELETE CASCADE,
    article_id      VARCHAR(30)     NOT NULL UNIQUE,
    title           TEXT            NOT NULL,
    is_pinned       BOOLEAN         DEFAULT false,
    author          VARCHAR(100),
    date            DATE,
    views           INTEGER         DEFAULT 0,
    category        VARCHAR(50),
    body_text       TEXT,
    source_url      TEXT,
    hash            CHAR(64)        NOT NULL,
    deadline        DATE,
    target          TEXT,
    apply_method    TEXT,
    ai_summary      JSONB,
    ai_processed_at TIMESTAMPTZ,
    crawled_at      TIMESTAMPTZ     DEFAULT now(),
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     DEFAULT now(),
    updated_at      TIMESTAMPTZ     DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notices_article_id   ON notices(article_id);
CREATE INDEX IF NOT EXISTS idx_notices_deadline     ON notices(deadline) WHERE deadline IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notices_ai_pending   ON notices(ai_processed_at) WHERE ai_processed_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_notices_category     ON notices(category);
CREATE INDEX IF NOT EXISTS idx_notices_published_at ON notices(published_at DESC NULLS LAST);
CREATE INDEX IF NOT EXISTS idx_notices_title_trgm   ON notices USING GIN (title gin_trgm_ops);

CREATE TABLE IF NOT EXISTS notice_attachments (
    id              BIGSERIAL       PRIMARY KEY,
    notice_id       BIGINT          NOT NULL REFERENCES notices(id) ON DELETE CASCADE,
    file_key        VARCHAR(500)    NOT NULL UNIQUE,
    original_name   VARCHAR(500)    NOT NULL,
    ext             VARCHAR(20),
    file_type       VARCHAR(20)     NOT NULL DEFAULT 'other'
                                    CHECK (file_type IN ('document', 'image', 'archive', 'other')),
    mime_type       VARCHAR(100),
    file_size       BIGINT,
    checksum        CHAR(64),
    source_url      TEXT,
    local_path      VARCHAR(500),
    download_ok     BOOLEAN         DEFAULT false,
    extracted_text  TEXT,
    created_at      TIMESTAMPTZ     DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_attachments_notice_id ON notice_attachments(notice_id);
CREATE INDEX IF NOT EXISTS idx_attachments_file_type ON notice_attachments(file_type);

CREATE TABLE IF NOT EXISTS raw_import_log (
    id              BIGSERIAL       PRIMARY KEY,
    file_name       VARCHAR(255)    NOT NULL,
    status          VARCHAR(20)     NOT NULL
                                    CHECK (status IN ('PENDING','SUCCESS','SKIPPED','FAILED','ERROR')),
    message         TEXT,
    processed_at    TIMESTAMPTZ     DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_raw_import_log_status       ON raw_import_log(status);
CREATE INDEX IF NOT EXISTS idx_raw_import_log_processed_at ON raw_import_log(processed_at DESC);

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = now(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_notices_updated_at
    BEFORE UPDATE ON notices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
