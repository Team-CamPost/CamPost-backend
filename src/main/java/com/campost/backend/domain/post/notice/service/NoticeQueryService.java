package com.campost.backend.domain.post.notice.service;

import com.campost.backend.domain.post.notice.dto.NoticeDetailDto;
import com.campost.backend.domain.post.notice.dto.NoticeDto;
import com.campost.backend.domain.post.notice.repository.NoticeQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NoticeQueryService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 100;

    private final NoticeQueryRepository noticeQueryRepository;

    public NoticeQueryService(NoticeQueryRepository noticeQueryRepository) {
        this.noticeQueryRepository = noticeQueryRepository;
    }

    public List<NoticeDto> getRecentNotices(int limit) {
        return noticeQueryRepository.findRecentNotices(normalizeLimit(limit));
    }

    public NoticeDetailDto getNoticeDetail(long noticeId) {
        return noticeQueryRepository.findNoticeDetailById(noticeId)
                .orElseThrow(() -> new NoSuchElementException("Notice not found: " + noticeId));
    }

    private int normalizeLimit(int limit) {
        if (limit < MIN_LIMIT) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
