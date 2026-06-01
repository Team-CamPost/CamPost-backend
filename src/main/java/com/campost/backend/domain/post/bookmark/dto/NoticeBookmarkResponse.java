package com.campost.backend.domain.post.bookmark.dto;

import com.campost.backend.domain.post.bookmark.model.NoticeBookmarkStatus;

public record NoticeBookmarkResponse(
        long noticeId,
        boolean bookmarked
) {
    public static NoticeBookmarkResponse from(NoticeBookmarkStatus status) {
        return new NoticeBookmarkResponse(status.noticeId(), status.bookmarked());
    }
}
