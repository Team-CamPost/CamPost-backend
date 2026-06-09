package com.campost.backend.domain.post.notice.repository;

import com.campost.backend.domain.post.notice.dto.NoticeDetailDto.AttachmentDto;
import com.campost.backend.domain.post.notice.dto.NoticeDetailDto;
import com.campost.backend.domain.post.notice.dto.NoticeDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NoticeQueryRepository {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    public NoticeQueryRepository(JdbcClient jdbcClient, ObjectMapper objectMapper) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
    }

    public List<NoticeDto> findNotices(int limit, String deptCode, String sortBy, Long userId) {
        boolean hasDeptCode = deptCode != null && !deptCode.isBlank();

        StringBuilder sql = new StringBuilder("""
                SELECT n.id, n.article_id, n.title, cs.department, n.author, n.category, n.date, n.views,
                       n.source_url,
                       (
                         SELECT image_asset ->> 'src'
                         FROM jsonb_array_elements(COALESCE(n.content_assets -> 'images', '[]'::jsonb))
                              WITH ORDINALITY AS images(image_asset, ordinal)
                         WHERE COALESCE((image_asset ->> 'file_size')::bigint, 0) >= 10000
                           AND COALESCE(image_asset ->> 'original_src', '') NOT ILIKE '%fonts.gstatic.com%'
                           AND NOT (
                             COALESCE(image_asset ->> 'original_src', '') LIKE 'data:image%'
                             AND COALESCE((image_asset ->> 'file_size')::bigint, 0) < 100000
                           )
                         ORDER BY
                             CASE
                               WHEN COALESCE(image_asset ->> 'name', '') ILIKE '%포스터%'
                                 OR COALESCE(image_asset ->> 'name', '') ILIKE '%poster%'
                               THEN 0
                               ELSE 1
                             END,
                           CASE WHEN image_asset ->> 'role' = 'body' THEN 0 ELSE 1 END,
                           ordinal
                         LIMIT 1
                       ) AS thumbnail_path,
                       n.deadline, n.target, n.apply_method, n.published_at, n.created_at,
                       CASE
                         WHEN CAST(:userId AS bigint) IS NULL THEN FALSE
                         ELSE EXISTS (
                           SELECT 1 FROM bookmarks b
                           WHERE b.notice_id = n.id AND b.user_id = CAST(:userId AS bigint)
                         )
                       END AS is_bookmarked
                FROM notices n
                LEFT JOIN raw_notices rn ON n.raw_notice_id = rn.id
                LEFT JOIN crawl_sources cs ON rn.source_id = cs.id
                WHERE 1=1
                """);

        if (hasDeptCode) {
            sql.append(" AND cs.dept_code = :deptCode ");
        }

        if ("deadline".equalsIgnoreCase(sortBy)) {
            // 마감 임박순 (이미 마감된 공지 제외)
            sql.append(" AND n.deadline IS NOT NULL AND n.deadline >= CURRENT_DATE ");
            sql.append(" ORDER BY n.deadline ASC NULLS LAST, n.id DESC ");
        } else {
            // 최신순 (기본값)
            sql.append(" ORDER BY COALESCE(n.published_at, n.crawled_at, n.created_at) DESC NULLS LAST, n.id DESC ");
        }

        sql.append(" LIMIT :limit");

        var query = jdbcClient.sql(sql.toString())
                .param("limit", limit)
                .param("userId", userId);

        if (hasDeptCode) {
            query = query.param("deptCode", deptCode);
        }

        return query.query((rs, rowNum) -> new NoticeDto(
                        rs.getLong("id"),
                        rs.getString("article_id"),
                        rs.getString("title"),
                        rs.getString("department"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getObject("date", java.time.LocalDate.class),
                        rs.getObject("views", Integer.class),
                        rs.getString("source_url"),
                        rs.getString("thumbnail_path"),
                        rs.getObject("deadline", java.time.LocalDate.class),
                        rs.getString("target"),
                        rs.getString("apply_method"),
                        rs.getObject("published_at", java.time.OffsetDateTime.class),
                        rs.getObject("created_at", java.time.OffsetDateTime.class),
                        rs.getBoolean("is_bookmarked")
                ))
                .list();
    }

    public List<NoticeDto> findBookmarkedNotices(long userId, int limit) {
        String sql = """
                SELECT n.id, n.article_id, n.title, cs.department, n.author, n.category, n.date, n.views,
                       n.source_url,
                       (
                         SELECT image_asset ->> 'src'
                         FROM jsonb_array_elements(COALESCE(n.content_assets -> 'images', '[]'::jsonb))
                              WITH ORDINALITY AS images(image_asset, ordinal)
                         WHERE COALESCE((image_asset ->> 'file_size')::bigint, 0) >= 10000
                           AND COALESCE(image_asset ->> 'original_src', '') NOT ILIKE '%fonts.gstatic.com%'
                           AND NOT (
                             COALESCE(image_asset ->> 'original_src', '') LIKE 'data:image%'
                             AND COALESCE((image_asset ->> 'file_size')::bigint, 0) < 100000
                           )
                         ORDER BY
                             CASE
                               WHEN COALESCE(image_asset ->> 'name', '') ILIKE '%포스터%'
                                 OR COALESCE(image_asset ->> 'name', '') ILIKE '%poster%'
                               THEN 0
                               ELSE 1
                             END,
                           CASE WHEN image_asset ->> 'role' = 'body' THEN 0 ELSE 1 END,
                           ordinal
                         LIMIT 1
                       ) AS thumbnail_path,
                       n.deadline, n.target, n.apply_method, n.published_at, n.created_at
                FROM notices n
                LEFT JOIN raw_notices rn ON n.raw_notice_id = rn.id
                LEFT JOIN crawl_sources cs ON rn.source_id = cs.id
                JOIN bookmarks b ON b.notice_id = n.id
                WHERE b.user_id = :userId
                ORDER BY b.created_at DESC NULLS LAST, n.id DESC
                LIMIT :limit
                """;

        return jdbcClient.sql(sql)
                .param("userId", userId)
                .param("limit", limit)
                .query((rs, rowNum) -> new NoticeDto(
                        rs.getLong("id"),
                        rs.getString("article_id"),
                        rs.getString("title"),
                        rs.getString("department"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getObject("date", java.time.LocalDate.class),
                        rs.getObject("views", Integer.class),
                        rs.getString("source_url"),
                        rs.getString("thumbnail_path"),
                        rs.getObject("deadline", java.time.LocalDate.class),
                        rs.getString("target"),
                        rs.getString("apply_method"),
                        rs.getObject("published_at", java.time.OffsetDateTime.class),
                        rs.getObject("created_at", java.time.OffsetDateTime.class),
                        true
                ))
                .list();
    }

    public Optional<NoticeDetailDto> findNoticeDetailById(long noticeId, Long userId) {
        String sql = """
                SELECT n.id, n.article_id, n.title, cs.department, n.author, n.category, n.date, n.views,
                       n.deadline, n.deadline_time, n.deadline_at,
                       n.target, n.apply_method, n.body_text, n.body_html,
                       n.content_html, n.content_assets::text AS content_assets,
                       n.content_stats::text AS content_stats,
                       n.source_url, n.published_at, n.created_at,
                       CASE
                         WHEN CAST(:userId AS bigint) IS NULL THEN FALSE
                         ELSE EXISTS (
                           SELECT 1 FROM bookmarks b
                           WHERE b.notice_id = n.id AND b.user_id = CAST(:userId AS bigint)
                         )
                       END AS is_bookmarked
                FROM notices n
              LEFT JOIN raw_notices rn ON n.raw_notice_id = rn.id
              LEFT JOIN crawl_sources cs ON rn.source_id = cs.id
                WHERE n.id = :noticeId
                """;

        return jdbcClient.sql(sql)
                .param("noticeId", noticeId)
                .param("userId", userId)
                .query((rs, rowNum) -> new NoticeDetailDto(
                        rs.getLong("id"),
                        rs.getString("article_id"),
                        rs.getString("title"),
                        rs.getString("department"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getObject("date", java.time.LocalDate.class),
                        rs.getObject("views", Integer.class),
                        rs.getObject("deadline", java.time.LocalDate.class),
                        rs.getObject("deadline_time", java.time.LocalTime.class),
                        rs.getObject("deadline_at", java.time.OffsetDateTime.class),
                        rs.getString("target"),
                        rs.getString("apply_method"),
                        rs.getString("body_text"),
                        rs.getString("body_html"),
                        rs.getString("content_html"),
                        readJson(rs.getString("content_assets")),
                        readJson(rs.getString("content_stats")),
                        findAttachmentsByNoticeId(noticeId),
                        rs.getString("source_url"),
                        rs.getObject("published_at", java.time.OffsetDateTime.class),
                        rs.getObject("created_at", java.time.OffsetDateTime.class),
                        rs.getBoolean("is_bookmarked")
                ))
                .optional();
    }

    private List<AttachmentDto> findAttachmentsByNoticeId(long noticeId) {
        String sql = """
                SELECT id, file_key, original_name, ext, file_type, mime_type, file_size,
                       checksum, source_url, local_path, r2_url, download_ok, extracted_text,
                       extracted_chars, parser, parse_quality, parse_ok, download_cached,
                       preview_pdf_path, preview_pdf_r2_url, preview_pdf_size, preview_pdf_checksum,
                       conversion_status, conversion_engine, conversion_error, created_at
                FROM notice_attachments
                WHERE notice_id = :noticeId
                ORDER BY id ASC
                """;

        return jdbcClient.sql(sql)
                .param("noticeId", noticeId)
                .query((rs, rowNum) -> new AttachmentDto(
                        rs.getLong("id"),
                        rs.getString("file_key"),
                        rs.getString("original_name"),
                        rs.getString("ext"),
                        rs.getString("file_type"),
                        rs.getString("mime_type"),
                        rs.getObject("file_size", Long.class),
                        rs.getString("checksum"),
                        rs.getString("source_url"),
                        rs.getString("local_path"),
                        rs.getString("r2_url"),
                        rs.getObject("download_ok", Boolean.class),
                        rs.getString("extracted_text"),
                        rs.getObject("extracted_chars", Integer.class),
                        rs.getString("parser"),
                        rs.getString("parse_quality"),
                        rs.getObject("parse_ok", Boolean.class),
                        rs.getObject("download_cached", Boolean.class),
                        rs.getString("preview_pdf_path"),
                        rs.getString("preview_pdf_r2_url"),
                        rs.getObject("preview_pdf_size", Long.class),
                        rs.getString("preview_pdf_checksum"),
                        rs.getString("conversion_status"),
                        rs.getString("conversion_engine"),
                        rs.getString("conversion_error"),
                        rs.getObject("created_at", java.time.OffsetDateTime.class)
                ))
                .list();
    }

    private JsonNode readJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to read notice JSON content.", ex);
        }
    }
}
