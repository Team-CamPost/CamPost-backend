package com.campost.backend.domain.post.bookmark.repository;

public interface NoticeBookmarkStore {

    boolean existsNoticeById(long noticeId);

    void save(long userId, long noticeId);

    void delete(long userId, long noticeId);
}
