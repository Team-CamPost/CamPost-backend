package com.campost.backend.features.importer.repository;

import com.campost.backend.features.importer.model.RawNoticePayload;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Repository
public class RawImporterRepository {

    private final JdbcClient jdbcClient;

    public RawImporterRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public long upsertRawNotice(RawNoticePayload payload, OffsetDateTime crawledAt) {
        String sql = """
                INSERT INTO raw_notices (
                    source_id, article_id, title, is_pinned, post_number,
                    author, date, views, has_attachment, category,
                    body_text, source_url, hash, crawled_at, parse_status
                ) VALUES (
                    :sourceId, :articleId, :title, :isPinned, :postNumber,
                    :author, :date, :views, :hasAttachment, :category,
                    :bodyText, :sourceUrl, :hash, :crawledAt, 'done'
                )
                ON CONFLICT (article_id) DO UPDATE SET
                    source_id = EXCLUDED.source_id,
                    title = EXCLUDED.title,
                    is_pinned = EXCLUDED.is_pinned,
                    post_number = EXCLUDED.post_number,
                    author = EXCLUDED.author,
                    date = EXCLUDED.date,
                    views = EXCLUDED.views,
                    has_attachment = EXCLUDED.has_attachment,
                    category = EXCLUDED.category,
                    body_text = EXCLUDED.body_text,
                    source_url = EXCLUDED.source_url,
                    hash = EXCLUDED.hash,
                    crawled_at = EXCLUDED.crawled_at,
                    parse_status = 'done'
                RETURNING id
                """;

        return jdbcClient.sql(sql)
                .param("sourceId", payload.sourceId())
                .param("articleId", payload.articleId())
                .param("title", payload.title())
                .param("isPinned", boolOrFalse(payload.isPinned()))
                .param("postNumber", payload.postNumber())
                .param("author", payload.author())
                .param("date", payload.date())
                .param("views", payload.views())
                .param("hasAttachment", boolOrFalse(payload.hasAttachment()))
                .param("category", payload.category())
                .param("bodyText", payload.bodyText())
                .param("sourceUrl", payload.sourceUrl())
                .param("hash", payload.hash())
                .param("crawledAt", crawledAt)
                .query(Long.class)
                .single();
    }

    public void upsertNotice(
            long rawNoticeId,
            RawNoticePayload payload,
            LocalDate noticeDate,
            Integer views,
            LocalDate deadline,
            OffsetDateTime crawledAt
    ) {
        String sql = """
                INSERT INTO notices (
                    raw_notice_id, article_id, title, is_pinned, author, date,
                    views, category, body_text, source_url, hash,
                    deadline, target, apply_method, crawled_at, published_at
                ) VALUES (
                    :rawNoticeId, :articleId, :title, :isPinned, :author, :date,
                    :views, :category, :bodyText, :sourceUrl, :hash,
                    :deadline, :target, :applyMethod, :crawledAt, :publishedAt
                )
                ON CONFLICT (article_id) DO UPDATE SET
                    raw_notice_id = EXCLUDED.raw_notice_id,
                    title = EXCLUDED.title,
                    is_pinned = EXCLUDED.is_pinned,
                    author = EXCLUDED.author,
                    date = EXCLUDED.date,
                    views = EXCLUDED.views,
                    category = EXCLUDED.category,
                    body_text = EXCLUDED.body_text,
                    source_url = EXCLUDED.source_url,
                    hash = EXCLUDED.hash,
                    deadline = EXCLUDED.deadline,
                    target = EXCLUDED.target,
                    apply_method = EXCLUDED.apply_method,
                    crawled_at = EXCLUDED.crawled_at,
                    published_at = EXCLUDED.published_at
                """;

        jdbcClient.sql(sql)
                .param("rawNoticeId", rawNoticeId)
                .param("articleId", payload.articleId())
                .param("title", payload.title())
                .param("isPinned", boolOrFalse(payload.isPinned()))
                .param("author", payload.author())
                .param("date", noticeDate)
                .param("views", views)
                .param("category", payload.category())
                .param("bodyText", payload.bodyText())
                .param("sourceUrl", payload.sourceUrl())
                .param("hash", payload.hash())
                .param("deadline", deadline)
                .param("target", payload.target())
                .param("applyMethod", payload.applyMethod())
                .param("crawledAt", crawledAt)
                .param("publishedAt", crawledAt)
                .update();
    }

    public void logImport(String fileName, String status, String message) {
        String sql = """
                INSERT INTO raw_import_log (file_name, status, message)
                VALUES (:fileName, :status, :message)
                """;

        jdbcClient.sql(sql)
                .param("fileName", fileName)
                .param("status", status)
                .param("message", message)
                .update();
    }

    private boolean boolOrFalse(Boolean value) {
        return value != null && value;
    }
}
