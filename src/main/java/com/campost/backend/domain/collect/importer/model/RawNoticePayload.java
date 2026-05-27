package com.campost.backend.domain.collect.importer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

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
        @JsonProperty("body_html") String bodyHtml,
        @JsonProperty("content_html") String contentHtml,
        @JsonProperty("content_assets") JsonNode contentAssets,
        @JsonProperty("content_stats") JsonNode contentStats,
        @JsonProperty("source_url") String sourceUrl,
        @JsonProperty("hash") String hash,
        @JsonProperty("crawled_at") String crawledAt,
        @JsonProperty("source_id") Long sourceId,
        @JsonProperty("deadline") String deadline,
        @JsonProperty("deadline_time") String deadlineTime,
        @JsonProperty("deadline_at") String deadlineAt,
        @JsonProperty("target") String target,
        @JsonProperty("apply_method") String applyMethod,
        @JsonProperty("attachments") List<RawAttachmentPayload> attachments
) {
    public List<RawAttachmentPayload> attachmentsOrEmpty() {
        return attachments == null ? List.of() : attachments;
    }

    public record RawAttachmentPayload(
            @JsonProperty("file_key") String fileKey,
            @JsonProperty("name") String name,
            @JsonProperty("ext") String ext,
            @JsonProperty("mime_type") String mimeType,
            @JsonProperty("file_size") Long fileSize,
            @JsonProperty("checksum") String checksum,
            @JsonProperty("url") String url,
            @JsonProperty("local_path") String localPath,
            @JsonProperty("download_ok") Boolean downloadOk,
            @JsonProperty("extracted_text") String extractedText,
            @JsonProperty("extracted_chars") Integer extractedChars,
            @JsonProperty("parser") String parser,
            @JsonProperty("parse_quality") String parseQuality,
            @JsonProperty("parse_ok") Boolean parseOk,
            @JsonProperty("download_cached") Boolean downloadCached,
            @JsonProperty("preview_pdf_path") String previewPdfPath,
            @JsonProperty("preview_pdf_size") Long previewPdfSize,
            @JsonProperty("preview_pdf_checksum") String previewPdfChecksum,
            @JsonProperty("conversion_status") String conversionStatus,
            @JsonProperty("conversion_engine") String conversionEngine,
            @JsonProperty("conversion_error") String conversionError
    ) {
    }
}
