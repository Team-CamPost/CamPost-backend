ALTER TABLE bookmarks
    ADD COLUMN IF NOT EXISTS notice_id BIGINT;

UPDATE bookmarks b
SET notice_id = n.id
FROM notices n
WHERE b.notice_id IS NULL
  AND b.article_id = n.article_id;

DELETE FROM bookmarks
WHERE notice_id IS NULL;

ALTER TABLE bookmarks
    ALTER COLUMN notice_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_bookmarks_notice_id'
    ) THEN
        ALTER TABLE bookmarks
            ADD CONSTRAINT fk_bookmarks_notice_id
            FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS ux_bookmarks_user_notice
    ON bookmarks(user_id, notice_id);

CREATE INDEX IF NOT EXISTS idx_bookmarks_notice_id
    ON bookmarks(notice_id);
