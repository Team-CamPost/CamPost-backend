package com.campost.backend.domain.collect.importer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RawNoticePayload(
        @JsonProperty("article_id") String articleId,
        @JsonProperty("title") String title,
        @JsonProperty("is_pinned") Boolean isPinned,
        @JsonProperty("post_number") String postNumber,
        @JsonProperty("author") String author,
        @JsonProperty("date") String date,
        @JsonProperty("views") String views,
        @JsonProperty("has_attachment") Boolean hasAttachment,
        @JsonProperty("category") String category,
        @JsonProperty("body_text") String bodyText,
        @JsonProperty("source_url") String sourceUrl,
        @JsonProperty("hash") String hash,
        @JsonProperty("crawled_at") String crawledAt,
        @JsonProperty("source_id") Long sourceId,
        @JsonProperty("deadline") String deadline,
        @JsonProperty("target") String target,
        @JsonProperty("apply_method") String applyMethod
) {
}
