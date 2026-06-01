package com.campost.backend.domain.post.bookmark.repository;

import java.util.Optional;

public interface NoticeBookmarkStore {

    Optional<String> findArticleIdByNoticeId(long noticeId);

    boolean existsNoticeById(long noticeId);

    void save(long userId, long noticeId, String articleId);

    boolean delete(long userId, long noticeId);
}
