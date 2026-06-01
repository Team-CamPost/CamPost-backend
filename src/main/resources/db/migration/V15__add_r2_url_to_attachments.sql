-- ============================================================
-- CamPost Notice Attachments - Cloudflare R2 URL fields
-- ============================================================

ALTER TABLE notice_attachments
    ADD COLUMN IF NOT EXISTS r2_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS preview_pdf_r2_url VARCHAR(500);
