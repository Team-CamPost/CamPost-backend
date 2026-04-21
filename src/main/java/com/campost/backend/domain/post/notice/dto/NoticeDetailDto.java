package com.campost.backend.domain.post.notice.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record NoticeDetailDto(
        long id,
        String articleId,
        String title,
        LocalDate date,
        String bodyText,
        String sourceUrl,
        OffsetDateTime publishedAt,
        OffsetDateTime createdAt
) {
}
