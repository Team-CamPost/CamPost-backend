package com.campost.backend.domain.post.notice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

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
        "deadline",
        "deadlineTime",
        "deadlineAt",
        "target",
        "applyMethod",
        "bodyText",
        "bodyHtml",
        "contentHtml",
        "contentAssets",
        "contentStats",
        "attachments",
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
        @JsonProperty("deadline")
        LocalDate deadline,
        @JsonProperty("deadlineTime")
        LocalTime deadlineTime,
        @JsonProperty("deadlineAt")
        OffsetDateTime deadlineAt,
        @JsonProperty("target")
        String target,
        @JsonProperty("applyMethod")
        String applyMethod,
        @JsonProperty("bodyText")
        String bodyText,
        @JsonProperty("bodyHtml")
        String bodyHtml,
        @JsonProperty("contentHtml")
        String contentHtml,
        @JsonProperty("contentAssets")
        JsonNode contentAssets,
        @JsonProperty("contentStats")
        JsonNode contentStats,
        @JsonProperty("attachments")
        List<AttachmentDto> attachments,
        @JsonProperty("sourceUrl")
        String sourceUrl,
        @JsonProperty("publishedAt")
        OffsetDateTime publishedAt,
        @JsonProperty("createdAt")
        OffsetDateTime createdAt
) {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonPropertyOrder({
            "id",
            "fileKey",
            "originalName",
            "ext",
            "fileType",
            "mimeType",
            "fileSize",
            "checksum",
            "sourceUrl",
            "localPath",
            "downloadOk",
            "extractedText",
            "extractedChars",
            "parser",
            "parseQuality",
            "parseOk",
            "downloadCached",
            "createdAt"
    })
    public record AttachmentDto(
            @JsonProperty("id")
            long id,
            @JsonProperty("fileKey")
            String fileKey,
            @JsonProperty("originalName")
            String originalName,
            @JsonProperty("ext")
            String ext,
            @JsonProperty("fileType")
            String fileType,
            @JsonProperty("mimeType")
            String mimeType,
            @JsonProperty("fileSize")
            Long fileSize,
            @JsonProperty("checksum")
            String checksum,
            @JsonProperty("sourceUrl")
            String sourceUrl,
            @JsonProperty("localPath")
            String localPath,
            @JsonProperty("downloadOk")
            Boolean downloadOk,
            @JsonProperty("extractedText")
            String extractedText,
            @JsonProperty("extractedChars")
            Integer extractedChars,
            @JsonProperty("parser")
            String parser,
            @JsonProperty("parseQuality")
            String parseQuality,
            @JsonProperty("parseOk")
            Boolean parseOk,
            @JsonProperty("downloadCached")
            Boolean downloadCached,
            @JsonProperty("createdAt")
            OffsetDateTime createdAt
    ) {
    }
}
