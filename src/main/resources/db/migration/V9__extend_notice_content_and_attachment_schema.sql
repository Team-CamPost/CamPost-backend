-- ============================================================
-- CamPost Post Domain - Rendered notice content and attachment metadata
-- ============================================================

ALTER TABLE raw_notices
    ADD COLUMN IF NOT EXISTS body_html TEXT,
    ADD COLUMN IF NOT EXISTS content_html TEXT,
    ADD COLUMN IF NOT EXISTS content_assets JSONB,
    ADD COLUMN IF NOT EXISTS content_stats JSONB,
    ADD COLUMN IF NOT EXISTS deadline_time TIME,
    ADD COLUMN IF NOT EXISTS deadline_at TIMESTAMPTZ;

ALTER TABLE notices
    ADD COLUMN IF NOT EXISTS body_html TEXT,
    ADD COLUMN IF NOT EXISTS content_html TEXT,
    ADD COLUMN IF NOT EXISTS content_assets JSONB,
    ADD COLUMN IF NOT EXISTS content_stats JSONB,
    ADD COLUMN IF NOT EXISTS deadline_time TIME,
    ADD COLUMN IF NOT EXISTS deadline_at TIMESTAMPTZ;

ALTER TABLE notice_attachments
    ADD COLUMN IF NOT EXISTS extracted_chars INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS parser VARCHAR(50),
    ADD COLUMN IF NOT EXISTS parse_quality VARCHAR(20) DEFAULT 'none',
    ADD COLUMN IF NOT EXISTS parse_ok BOOLEAN DEFAULT false,
    ADD COLUMN IF NOT EXISTS download_cached BOOLEAN DEFAULT false;

ALTER TABLE notice_attachments
    DROP CONSTRAINT IF EXISTS notice_attachments_parse_quality_check;

ALTER TABLE notice_attachments
    ADD CONSTRAINT notice_attachments_parse_quality_check
    CHECK (parse_quality IN ('full', 'preview', 'none'));

CREATE INDEX IF NOT EXISTS idx_raw_notices_deadline_at
ON raw_notices(deadline_at) WHERE deadline_at IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_notices_deadline_at
ON notices(deadline_at) WHERE deadline_at IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_attachments_parse_quality
ON notice_attachments(parse_quality);
