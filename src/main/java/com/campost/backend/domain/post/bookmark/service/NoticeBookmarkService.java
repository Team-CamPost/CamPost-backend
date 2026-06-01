package com.campost.backend.domain.post.bookmark.service;

import com.campost.backend.domain.post.bookmark.model.NoticeBookmarkStatus;
import com.campost.backend.domain.post.bookmark.repository.NoticeBookmarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class NoticeBookmarkService {

    private final NoticeBookmarkRepository noticeBookmarkRepository;

    public NoticeBookmarkService(NoticeBookmarkRepository noticeBookmarkRepository) {
        this.noticeBookmarkRepository = noticeBookmarkRepository;
    }

    @Transactional
    public NoticeBookmarkStatus bookmark(long userId, long noticeId) {
        String articleId = noticeBookmarkRepository.findArticleIdByNoticeId(noticeId)
                .orElseThrow(() -> new NoSuchElementException("Notice not found: " + noticeId));

        noticeBookmarkRepository.save(userId, noticeId, articleId);

        return new NoticeBookmarkStatus(noticeId, true);
    }

    @Transactional
    public NoticeBookmarkStatus unbookmark(long userId, long noticeId) {
        if (!noticeBookmarkRepository.existsNoticeById(noticeId)) {
            throw new NoSuchElementException("Notice not found: " + noticeId);
        }

        noticeBookmarkRepository.delete(userId, noticeId);

        return new NoticeBookmarkStatus(noticeId, false);
    }
}
