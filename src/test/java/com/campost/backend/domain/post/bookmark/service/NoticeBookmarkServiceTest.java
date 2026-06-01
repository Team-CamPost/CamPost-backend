package com.campost.backend.domain.post.bookmark.service;

import com.campost.backend.domain.post.bookmark.model.NoticeBookmarkStatus;
import com.campost.backend.domain.post.bookmark.repository.NoticeBookmarkStore;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoticeBookmarkServiceTest {

    private final FakeNoticeBookmarkStore noticeBookmarkStore = new FakeNoticeBookmarkStore();
    private final NoticeBookmarkService noticeBookmarkService = new NoticeBookmarkService(noticeBookmarkStore);

    @Test
    void bookmarkSavesNoticeBookmark() {
        noticeBookmarkStore.noticeExists = true;

        NoticeBookmarkStatus status = noticeBookmarkService.bookmark(1L, 10L);

        assertThat(status.noticeId()).isEqualTo(10L);
        assertThat(status.bookmarked()).isTrue();
        assertThat(noticeBookmarkStore.savedUserId).isEqualTo(1L);
        assertThat(noticeBookmarkStore.savedNoticeId).isEqualTo(10L);
    }

    @Test
    void bookmarkThrowsExceptionWhenNoticeDoesNotExist() {
        noticeBookmarkStore.noticeExists = false;

        assertThatThrownBy(() -> noticeBookmarkService.bookmark(1L, 999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void unbookmarkDeletesNoticeBookmark() {
        noticeBookmarkStore.noticeExists = true;

        NoticeBookmarkStatus status = noticeBookmarkService.unbookmark(1L, 10L);

        assertThat(status.noticeId()).isEqualTo(10L);
        assertThat(status.bookmarked()).isFalse();
        assertThat(noticeBookmarkStore.deletedUserId).isEqualTo(1L);
        assertThat(noticeBookmarkStore.deletedNoticeId).isEqualTo(10L);
    }

    @Test
    void unbookmarkThrowsExceptionWhenNoticeDoesNotExist() {
        noticeBookmarkStore.noticeExists = false;

        assertThatThrownBy(() -> noticeBookmarkService.unbookmark(1L, 999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    private static class FakeNoticeBookmarkStore implements NoticeBookmarkStore {

        private boolean noticeExists;
        private long savedUserId;
        private long savedNoticeId;
        private long deletedUserId;
        private long deletedNoticeId;

        @Override
        public boolean existsNoticeById(long noticeId) {
            return noticeExists;
        }

        @Override
        public void save(long userId, long noticeId) {
            this.savedUserId = userId;
            this.savedNoticeId = noticeId;
        }

        @Override
        public void delete(long userId, long noticeId) {
            this.deletedUserId = userId;
            this.deletedNoticeId = noticeId;
        }
    }
}
