-- ============================================================
-- CamPost DB 스키마 v4
-- 기준: 공통스키마 v3 (Notion 2026-04-01) + 다중 크롤러 확장
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- ============================================================
-- 1. crawl_sources — 크롤링 대상 (Backend Admin 소유, Pipeline 읽기)
-- ============================================================
CREATE TABLE IF NOT EXISTS crawl_sources (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    base_url        TEXT            NOT NULL,
    department      VARCHAR(100),
    crawler_type    VARCHAR(10)     NOT NULL DEFAULT 'card'
                                    CHECK (crawler_type IN ('card', 'table')),
    dept_code       VARCHAR(20)     NOT NULL UNIQUE,
    is_active       BOOLEAN         DEFAULT true,
    created_at      TIMESTAMPTZ     DEFAULT now(),
    updated_at      TIMESTAMPTZ     DEFAULT now()
);

INSERT INTO crawl_sources (name, base_url, department, crawler_type, dept_code) VALUES
  ('소프트웨어학과',     'https://cms.dankook.ac.kr/web/sw/-1',            '소프트웨어학과',     'card',  'SW'),
  ('컴퓨터공학과',       'https://cms.dankook.ac.kr/web/ace/notice',       '컴퓨터공학과',       'card',  'ACE'),
  ('모바일시스템공학과', 'https://cms.dankook.ac.kr/web/mobilesystems/-8', '모바일시스템공학과', 'card',  'MOBILE'),
  ('통계사이언스학과',   'https://cms.dankook.ac.kr/web/dkustat/-6',       '통계사이언스학과',   'card',  'STAT'),
  ('사이버보안학과',     'https://cms.dankook.ac.kr/web/indsec/-4',        '사이버보안학과',     'card',  'INDSEC'),
  ('SW중심대학사업단',   'https://swcu.dankook.ac.kr/en/-5',               'SW중심대학사업단',   'card',  'SWCU')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 2. crawl_jobs — 크롤링 실행 이력 (Pipeline 직접 쓰기)
-- ============================================================
CREATE TABLE IF NOT EXISTS crawl_jobs (
    id              BIGSERIAL       PRIMARY KEY,
    source_id       BIGINT          NOT NULL REFERENCES crawl_sources(id),
    started_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    finished_at     TIMESTAMPTZ,
    status          VARCHAR(20)     NOT NULL DEFAULT 'running'
                                    CHECK (status IN ('running', 'success', 'failed')),
    total_found     INTEGER         DEFAULT 0,
    new_count       INTEGER         DEFAULT 0,
    skip_count      INTEGER         DEFAULT 0,
    fail_count      INTEGER         DEFAULT 0,
    error_msg       TEXT
);

CREATE INDEX IF NOT EXISTS idx_crawl_jobs_source_id  ON crawl_jobs(source_id);
CREATE INDEX IF NOT EXISTS idx_crawl_jobs_started_at ON crawl_jobs(started_at DESC);

-- ============================================================
-- 3. parse_logs — 첨부파일 파싱 이력 (Pipeline 직접 쓰기)
-- ============================================================
CREATE TABLE IF NOT EXISTS parse_logs (
    id              BIGSERIAL       PRIMARY KEY,
    crawl_job_id    BIGINT          REFERENCES crawl_jobs(id) ON DELETE SET NULL,
    file_key        VARCHAR(500)    NOT NULL,
    parser          VARCHAR(30)     NOT NULL
                                    CHECK (parser IN ('pdfplumber', 'olefile', 'hwpx_xml', 'none')),
    success         BOOLEAN         NOT NULL,
    chars_extracted INTEGER         DEFAULT 0,
    error_msg       TEXT,
    parsed_at       TIMESTAMPTZ     DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_parse_logs_crawl_job_id ON parse_logs(crawl_job_id);
CREATE INDEX IF NOT EXISTS idx_parse_logs_file_key     ON parse_logs(file_key);

-- ============================================================
-- 4. raw_notices — Raw JSON 적재 원본 (Importer 소유)
-- ============================================================
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

-- ============================================================
-- 5. notices — 서비스용 공지 (Importer 소유)
-- ============================================================
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

    -- rule-based extractor 결과 (AI 처리 전에도 채워짐)
    deadline        DATE,
    target          TEXT,
    apply_method    TEXT,

    -- AI 요약 (Sprint 2)
    ai_summary      JSONB,
    -- {"summary":["1줄","2줄","3줄"],"deadline":"YYYY-MM-DD","target":"...","apply_method":"...","benefit":"..."}
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

-- ============================================================
-- 6. notice_attachments — 첨부파일 (Importer 소유)
-- ============================================================
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

-- ============================================================
-- 7. raw_import_log — JSON 임포트 이력 (Importer 소유)
-- ============================================================
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

-- ============================================================
-- 8. users (Backend API 소유)
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

-- ============================================================
-- 9. bookmarks (Backend API 소유)
-- ============================================================
CREATE TABLE IF NOT EXISTS bookmarks (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    article_id      VARCHAR(30)     NOT NULL,
    created_at      TIMESTAMPTZ     DEFAULT now(),
    UNIQUE (user_id, article_id)
);

CREATE INDEX IF NOT EXISTS idx_bookmarks_user_id ON bookmarks(user_id);

-- ============================================================
-- 10. keywords (Backend API 소유)
-- ============================================================
CREATE TABLE IF NOT EXISTS keywords (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    keyword         VARCHAR(50)     NOT NULL,
    created_at      TIMESTAMPTZ     DEFAULT now(),
    UNIQUE (user_id, keyword)
);

CREATE INDEX IF NOT EXISTS idx_keywords_user_id ON keywords(user_id);

-- ============================================================
-- 11. notifications (Backend API 소유)
-- ============================================================
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

-- ============================================================
-- updated_at 트리거 (notices)
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = now(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_notices_updated_at
    BEFORE UPDATE ON notices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ============================================================
-- 기본 Admin 계정 (비밀번호: admin1234 BCrypt)
-- ============================================================
INSERT INTO users (email, password_hash, name, role)
VALUES ('admin@dankook.ac.kr',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'CamPost Admin', 'ADMIN')
ON CONFLICT DO NOTHING;
