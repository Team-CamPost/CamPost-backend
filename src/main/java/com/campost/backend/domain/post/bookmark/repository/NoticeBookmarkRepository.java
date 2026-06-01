package com.campost.backend.domain.post.bookmark.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class NoticeBookmarkRepository implements NoticeBookmarkStore {

    private final JdbcClient jdbcClient;

    public NoticeBookmarkRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<String> findArticleIdByNoticeId(long noticeId) {
        String sql = """
                SELECT article_id
                FROM notices
                WHERE id = :noticeId
                """;

        return jdbcClient.sql(sql)
                .param("noticeId", noticeId)
                .query(String.class)
                .optional();
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
    public void save(long userId, long noticeId, String articleId) {
        String sql = """
                INSERT INTO bookmarks (user_id, notice_id, article_id)
                VALUES (:userId, :noticeId, :articleId)
                ON CONFLICT (user_id, notice_id) DO NOTHING
                """;

        jdbcClient.sql(sql)
                .param("userId", userId)
                .param("noticeId", noticeId)
                .param("articleId", articleId)
                .update();
    }

    @Override
    public boolean delete(long userId, long noticeId) {
        String sql = """
                DELETE FROM bookmarks
                WHERE user_id = :userId
                  AND notice_id = :noticeId
                """;

        int updatedRows = jdbcClient.sql(sql)
                .param("userId", userId)
                .param("noticeId", noticeId)
                .update();

        return updatedRows > 0;
    }
}
