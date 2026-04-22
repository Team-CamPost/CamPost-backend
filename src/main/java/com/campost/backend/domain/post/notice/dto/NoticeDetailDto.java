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
        "author",
        "category",
        "date",
        "views",
        "deadline",
        "target",
        "applyMethod",
        "bodyText",
        "sourceUrl",
        "publishedAt",
        "createdAt"
})
public record NoticeDetailDto(
        @JsonProperty("id")
        long id,
        @JsonProperty("articleId")
        String articleId,
        @JsonProperty("title")
        String title,
        @JsonProperty("author")
        String author,
        @JsonProperty("category")
        String category,
        @JsonProperty("date")
        LocalDate date,
        @JsonProperty("views")
        Integer views,
        @JsonProperty("deadline")
        LocalDate deadline,
        @JsonProperty("target")
        String target,
        @JsonProperty("applyMethod")
        String applyMethod,
        @JsonProperty("bodyText")
        String bodyText,
        @JsonProperty("sourceUrl")
        String sourceUrl,
        @JsonProperty("publishedAt")
        OffsetDateTime publishedAt,
        @JsonProperty("createdAt")
        OffsetDateTime createdAt
) {
}
