package com.campost.backend.features.notices.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record NoticeDto(
        long id,
        String articleId,
        String title,
        String author,
        String category,
        LocalDate date,
        Integer views,
        String sourceUrl,
        LocalDate deadline,
        String target,
        String applyMethod,
        OffsetDateTime publishedAt,
        OffsetDateTime createdAt
) {
}
