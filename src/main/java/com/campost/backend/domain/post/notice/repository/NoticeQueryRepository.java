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

    public List<NoticeDto> findNotices(int limit, String deptCode, String sortBy) {
        boolean hasDeptCode = deptCode != null && !deptCode.isBlank();

        StringBuilder sql = new StringBuilder("""
                SELECT n.id, n.article_id, n.title, cs.department, n.author, n.category, n.date, n.views,
                       n.source_url, n.deadline, n.target, n.apply_method, n.published_at, n.created_at
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
                .param("limit", limit);

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
                        rs.getObject("deadline", java.time.LocalDate.class),
                        rs.getString("target"),
                        rs.getString("apply_method"),
                        rs.getObject("published_at", java.time.OffsetDateTime.class),
                        rs.getObject("created_at", java.time.OffsetDateTime.class)
                ))
                .list();
    }

    public Optional<NoticeDetailDto> findNoticeDetailById(long noticeId) {
        String sql = """
                SELECT n.id, n.article_id, n.title, cs.department, n.author, n.category, n.date, n.views,
                       n.deadline, n.deadline_time, n.deadline_at,
                       n.target, n.apply_method, n.body_text, n.body_html,
                       n.content_html, n.content_assets::text AS content_assets,
                       n.content_stats::text AS content_stats,
                       n.source_url, n.published_at, n.created_at
                FROM notices n
              LEFT JOIN raw_notices rn ON n.raw_notice_id = rn.id
              LEFT JOIN crawl_sources cs ON rn.source_id = cs.id
                WHERE n.id = :noticeId
                """;

        return jdbcClient.sql(sql)
                .param("noticeId", noticeId)
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
                        rs.getObject("created_at", java.time.OffsetDateTime.class)
                ))
                .optional();
    }

    private List<AttachmentDto> findAttachmentsByNoticeId(long noticeId) {
        String sql = """
                SELECT id, file_key, original_name, ext, file_type, mime_type, file_size,
                       checksum, source_url, local_path, download_ok, extracted_text,
                       extracted_chars, parser, parse_quality, parse_ok, download_cached, created_at
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
                        rs.getObject("download_ok", Boolean.class),
                        rs.getString("extracted_text"),
                        rs.getObject("extracted_chars", Integer.class),
                        rs.getString("parser"),
                        rs.getString("parse_quality"),
                        rs.getObject("parse_ok", Boolean.class),
                        rs.getObject("download_cached", Boolean.class),
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
