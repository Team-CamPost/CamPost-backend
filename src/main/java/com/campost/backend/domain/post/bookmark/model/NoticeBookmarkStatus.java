package com.campost.backend.domain.post.bookmark.model;

public record NoticeBookmarkStatus(
        long noticeId,
        boolean bookmarked
) {
}
