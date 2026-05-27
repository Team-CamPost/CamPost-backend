-- ============================================================
-- CamPost Notice Attachments - PDF preview metadata
-- ============================================================

ALTER TABLE notice_attachments
    ADD COLUMN IF NOT EXISTS preview_pdf_path VARCHAR(500),
    ADD COLUMN IF NOT EXISTS preview_pdf_size BIGINT,
    ADD COLUMN IF NOT EXISTS preview_pdf_checksum CHAR(64),
    ADD COLUMN IF NOT EXISTS conversion_status VARCHAR(20) DEFAULT 'not_applicable',
    ADD COLUMN IF NOT EXISTS conversion_engine VARCHAR(50),
    ADD COLUMN IF NOT EXISTS conversion_error TEXT;

ALTER TABLE notice_attachments
    DROP CONSTRAINT IF EXISTS notice_attachments_conversion_status_check;

ALTER TABLE notice_attachments
    ADD CONSTRAINT notice_attachments_conversion_status_check
    CHECK (
        conversion_status IN (
            'success',
            'failed',
            'timeout',
            'unavailable',
            'disabled',
            'download_failed',
            'not_applicable'
        )
    );

CREATE INDEX IF NOT EXISTS idx_attachments_conversion_status
ON notice_attachments(conversion_status);
