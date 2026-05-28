package com.campost.backend.domain.post.notice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
        "id",
        "articleId",
        "title",
        "department",
        "author",
        "category",
        "date",
        "views",
        "sourceUrl",
        "thumbnailPath",
        "deadline",
        "target",
        "applyMethod",
        "publishedAt",
        "createdAt"
})
public record NoticeDto(
        @JsonProperty("id")
        long id,
        @JsonProperty("articleId")
        String articleId,
        @JsonProperty("title")
        String title,
        @JsonProperty("department")
        String department,
        @JsonProperty("author")
        String author,
        @JsonProperty("category")
        String category,
        @JsonProperty("date")
        LocalDate date,
        @JsonProperty("views")
        Integer views,
        @JsonProperty("sourceUrl")
        String sourceUrl,
        @JsonProperty("thumbnailPath")
        String thumbnailPath,
        @JsonProperty("deadline")
        LocalDate deadline,
        @JsonProperty("target")
        String target,
        @JsonProperty("applyMethod")
        String applyMethod,
        @JsonProperty("publishedAt")
        OffsetDateTime publishedAt,
        @JsonProperty("createdAt")
        OffsetDateTime createdAt
) {
}
