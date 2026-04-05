package com.campost.backend.features.notices.service;

import com.campost.backend.features.notices.dto.NoticeDto;
import com.campost.backend.features.notices.repository.NoticeQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeQueryService {

    private static final int MAX_LIMIT = 100;

    private final NoticeQueryRepository noticeQueryRepository;

    public NoticeQueryService(NoticeQueryRepository noticeQueryRepository) {
        this.noticeQueryRepository = noticeQueryRepository;
    }

    public List<NoticeDto> getRecentNotices(int limit) {
        int safeLimit = normalizeLimit(limit);
        return noticeQueryRepository.findRecentNotices(safeLimit);
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 20;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
