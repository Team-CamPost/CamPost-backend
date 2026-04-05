-- ============================================================
-- CamPost Collect Domain Schema
-- - crawl_sources
-- - crawl_jobs
-- - parse_logs
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
