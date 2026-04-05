package com.campost.backend.features.crawl.dto;

import java.time.OffsetDateTime;

public record CrawlJobDto(
        long id,
        long sourceId,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        String status,
        int totalFound,
        int newCount,
        int skipCount,
        int failCount,
        String errorMsg
) {
}
