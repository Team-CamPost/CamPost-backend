-- ============================================================
-- CamPost Post Domain - Notice list ordering index
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_notices_recent_order
ON notices ((COALESCE(published_at, crawled_at, created_at)) DESC NULLS LAST, id DESC);
