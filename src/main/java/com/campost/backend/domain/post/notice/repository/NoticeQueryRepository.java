package com.campost.backend.domain.post.notice.repository;

import com.campost.backend.domain.post.notice.dto.NoticeDetailDto;
import com.campost.backend.domain.post.notice.dto.NoticeDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NoticeQueryRepository {

    private final JdbcClient jdbcClient;

    public NoticeQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<NoticeDto> findRecentNotices(int limit) {
        String sql = """
                SELECT id, article_id, title, author, category, date, views,
                       source_url, deadline, target, apply_method, published_at, created_at
                FROM notices
                                ORDER BY COALESCE(published_at, crawled_at, created_at) DESC NULLS LAST, id DESC
                LIMIT :limit
                """;

        return jdbcClient.sql(sql)
                .param("limit", limit)
                .query((rs, rowNum) -> new NoticeDto(
                        rs.getLong("id"),
                        rs.getString("article_id"),
                        rs.getString("title"),
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
                SELECT id, article_id, title, author, category, date, views,
                       deadline, target, apply_method, body_text, source_url, published_at, created_at
                FROM notices
                WHERE id = :noticeId
                """;

        return jdbcClient.sql(sql)
                .param("noticeId", noticeId)
                .query((rs, rowNum) -> new NoticeDetailDto(
                        rs.getLong("id"),
                        rs.getString("article_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getObject("date", java.time.LocalDate.class),
                        rs.getObject("views", Integer.class),
                        rs.getObject("deadline", java.time.LocalDate.class),
                        rs.getString("target"),
                        rs.getString("apply_method"),
                        rs.getString("body_text"),
                        rs.getString("source_url"),
                        rs.getObject("published_at", java.time.OffsetDateTime.class),
                        rs.getObject("created_at", java.time.OffsetDateTime.class)
                ))
                .optional();
    }
}
