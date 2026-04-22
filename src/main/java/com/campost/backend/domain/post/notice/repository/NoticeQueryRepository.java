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

    public List<NoticeDto> findNotices(int limit, String deptCode, String sortBy) {
        StringBuilder sql = new StringBuilder("""
                SELECT n.id, n.article_id, n.title, cs.department, n.author, n.category, n.date, n.views,
                       n.source_url, n.deadline, n.target, n.apply_method, n.published_at, n.created_at
                FROM notices n
                JOIN raw_notices rn ON n.raw_notice_id = rn.id
                JOIN crawl_sources cs ON rn.source_id = cs.id
                WHERE 1=1
                """);

        if (deptCode != null && !deptCode.isBlank()) {
            sql.append(" AND cs.dept_code = :deptCode ");
        }

        if ("deadline".equalsIgnoreCase(sortBy)) {
            // 마감 임박순 (과거 마감일 제외 추가 가능, 일단 정렬만)
            sql.append(" ORDER BY n.deadline ASC NULLS LAST, n.id DESC ");
        } else {
            // 최신순 (기본값)
            sql.append(" ORDER BY COALESCE(n.published_at, n.crawled_at, n.created_at) DESC NULLS LAST, n.id DESC ");
        }

        sql.append(" LIMIT :limit");

        var query = jdbcClient.sql(sql.toString())
                .param("limit", limit);

        if (deptCode != null && !deptCode.isBlank()) {
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
                       n.deadline, n.target, n.apply_method, n.body_text, n.source_url, n.published_at, n.created_at
                FROM notices n
                JOIN raw_notices rn ON n.raw_notice_id = rn.id
                JOIN crawl_sources cs ON rn.source_id = cs.id
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
