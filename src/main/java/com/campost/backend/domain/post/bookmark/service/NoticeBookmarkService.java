package com.campost.backend.domain.post.bookmark.service;

import com.campost.backend.domain.post.bookmark.model.NoticeBookmarkStatus;
import com.campost.backend.domain.post.bookmark.repository.NoticeBookmarkStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class NoticeBookmarkService {

    private final NoticeBookmarkStore noticeBookmarkStore;

    public NoticeBookmarkService(NoticeBookmarkStore noticeBookmarkStore) {
        this.noticeBookmarkStore = noticeBookmarkStore;
    }

    @Transactional
    public NoticeBookmarkStatus bookmark(long userId, long noticeId) {
        String articleId = noticeBookmarkStore.findArticleIdByNoticeId(noticeId)
                .orElseThrow(() -> new NoSuchElementException("Notice not found: " + noticeId));

        noticeBookmarkStore.save(userId, noticeId, articleId);

        return new NoticeBookmarkStatus(noticeId, true);
    }

    @Transactional
    public NoticeBookmarkStatus unbookmark(long userId, long noticeId) {
        if (!noticeBookmarkStore.existsNoticeById(noticeId)) {
            throw new NoSuchElementException("Notice not found: " + noticeId);
        }

        noticeBookmarkStore.delete(userId, noticeId);

        return new NoticeBookmarkStatus(noticeId, false);
    }
}
