package com.campost.backend.features.crawl.dto;

import java.time.OffsetDateTime;

public record ParseLogDto(
        long id,
        Long crawlJobId,
        String fileKey,
        String parser,
        boolean success,
        int charsExtracted,
        String errorMsg,
        OffsetDateTime parsedAt
) {
}
