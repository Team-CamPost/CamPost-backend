package com.campost.backend.domain.post.bookmark.service;

import com.campost.backend.domain.post.bookmark.model.NoticeBookmarkStatus;
import com.campost.backend.domain.post.bookmark.repository.NoticeBookmarkRepository;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoticeBookmarkServiceTest {

    private final FakeNoticeBookmarkRepository noticeBookmarkRepository = new FakeNoticeBookmarkRepository();
    private final NoticeBookmarkService noticeBookmarkService = new NoticeBookmarkService(noticeBookmarkRepository);

    @Test
    void bookmarkSavesNoticeBookmark() {
        noticeBookmarkRepository.articleId = Optional.of("12345");

        NoticeBookmarkStatus status = noticeBookmarkService.bookmark(1L, 10L);

        assertThat(status.noticeId()).isEqualTo(10L);
        assertThat(status.bookmarked()).isTrue();
        assertThat(noticeBookmarkRepository.savedUserId).isEqualTo(1L);
        assertThat(noticeBookmarkRepository.savedNoticeId).isEqualTo(10L);
        assertThat(noticeBookmarkRepository.savedArticleId).isEqualTo("12345");
    }

    @Test
    void bookmarkThrowsExceptionWhenNoticeDoesNotExist() {
        noticeBookmarkRepository.articleId = Optional.empty();

        assertThatThrownBy(() -> noticeBookmarkService.bookmark(1L, 999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void unbookmarkDeletesNoticeBookmark() {
        noticeBookmarkRepository.noticeExists = true;

        NoticeBookmarkStatus status = noticeBookmarkService.unbookmark(1L, 10L);

        assertThat(status.noticeId()).isEqualTo(10L);
        assertThat(status.bookmarked()).isFalse();
        assertThat(noticeBookmarkRepository.deletedUserId).isEqualTo(1L);
        assertThat(noticeBookmarkRepository.deletedNoticeId).isEqualTo(10L);
    }

    @Test
    void unbookmarkThrowsExceptionWhenNoticeDoesNotExist() {
        noticeBookmarkRepository.noticeExists = false;

        assertThatThrownBy(() -> noticeBookmarkService.unbookmark(1L, 999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    private static class FakeNoticeBookmarkRepository extends NoticeBookmarkRepository {

        private Optional<String> articleId = Optional.empty();
        private boolean noticeExists;
        private long savedUserId;
        private long savedNoticeId;
        private String savedArticleId;
        private long deletedUserId;
        private long deletedNoticeId;

        private FakeNoticeBookmarkRepository() {
            super(null);
        }

        @Override
        public Optional<String> findArticleIdByNoticeId(long noticeId) {
            return articleId;
        }

        @Override
        public boolean existsNoticeById(long noticeId) {
            return noticeExists;
        }

        @Override
        public void save(long userId, long noticeId, String articleId) {
            this.savedUserId = userId;
            this.savedNoticeId = noticeId;
            this.savedArticleId = articleId;
        }

        @Override
        public boolean delete(long userId, long noticeId) {
            this.deletedUserId = userId;
            this.deletedNoticeId = noticeId;
            return true;
        }
    }
}
