package com.campost.backend.domain.post.bookmark.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class NoticeBookmarkRepository implements NoticeBookmarkStore {

    private final JdbcClient jdbcClient;

    public NoticeBookmarkRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public boolean existsNoticeById(long noticeId) {
        String sql = """
                SELECT 1
                FROM notices
                WHERE id = :noticeId
                """;

        return jdbcClient.sql(sql)
                .param("noticeId", noticeId)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    @Override
    public void save(long userId, long noticeId) {
        String sql = """
                INSERT INTO bookmarks (user_id, notice_id)
                VALUES (:userId, :noticeId)
                ON CONFLICT (user_id, notice_id) DO NOTHING
                """;

        jdbcClient.sql(sql)
                .param("userId", userId)
                .param("noticeId", noticeId)
                .update();
    }

    @Override
    public void delete(long userId, long noticeId) {
        String sql = """
                DELETE FROM bookmarks
                WHERE user_id = :userId
                  AND notice_id = :noticeId
                """;

        jdbcClient.sql(sql)
                .param("userId", userId)
                .param("noticeId", noticeId)
                .update();
    }
}
