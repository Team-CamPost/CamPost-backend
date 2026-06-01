package com.campost.backend.domain.collect.importer.repository;

import com.campost.backend.domain.collect.importer.model.RawNoticePayload;
import com.campost.backend.domain.collect.importer.model.RawNoticePayload.RawAttachmentPayload;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Repository
public class RawImporterRepository {

    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            "pdf", "hwp", "hwpx", "doc", "docx", "ppt", "pptx", "xls", "xlsx"
    );
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "webp", "bmp", "svg"
    );
    private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
            "zip", "7z", "rar", "tar", "gz"
    );
    private static final Set<String> CONVERSION_STATUSES = Set.of(
            "success",
            "failed",
            "timeout",
            "unavailable",
            "disabled",
            "download_failed",
            "not_applicable"
    );

    private final JdbcClient jdbcClient;

    public RawImporterRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public long upsertRawNotice(
            RawNoticePayload payload,
            OffsetDateTime crawledAt,
            LocalTime deadlineTime,
            OffsetDateTime deadlineAt
    ) {
        String sql = """
                INSERT INTO raw_notices (
                    source_id, article_id, title, is_pinned, post_number,
                    author, date, views, has_attachment, category,
                    body_text, body_html, content_html, content_assets, content_stats,
                    source_url, hash, crawled_at, parse_status,
                    deadline_time, deadline_at
                ) VALUES (
                    :sourceId, :articleId, :title, :isPinned, :postNumber,
                    :author, :date, :views, :hasAttachment, :category,
                    :bodyText, :bodyHtml, :contentHtml, :contentAssets, :contentStats,
                    :sourceUrl, :hash, :crawledAt, 'done',
                    :deadlineTime, :deadlineAt
                )
                ON CONFLICT (article_id) DO UPDATE SET
                    source_id = EXCLUDED.source_id,
                    title = EXCLUDED.title,
                    is_pinned = EXCLUDED.is_pinned,
                    post_number = EXCLUDED.post_number,
                    author = EXCLUDED.author,
                    date = EXCLUDED.date,
                    views = EXCLUDED.views,
                    has_attachment = EXCLUDED.has_attachment,
                    category = EXCLUDED.category,
                    body_text = EXCLUDED.body_text,
                    body_html = EXCLUDED.body_html,
                    content_html = EXCLUDED.content_html,
                    content_assets = EXCLUDED.content_assets,
                    content_stats = EXCLUDED.content_stats,
                    source_url = EXCLUDED.source_url,
                    hash = EXCLUDED.hash,
                    crawled_at = EXCLUDED.crawled_at,
                    parse_status = 'done',
                    deadline_time = EXCLUDED.deadline_time,
                    deadline_at = EXCLUDED.deadline_at
                RETURNING id
                """;

        return jdbcClient.sql(sql)
                .param("sourceId", payload.sourceId())
                .param("articleId", payload.articleId())
                .param("title", payload.title())
                .param("isPinned", boolOrFalse(payload.isPinned()))
                .param("postNumber", payload.postNumber())
                .param("author", payload.author())
                .param("date", payload.date())
                .param("views", payload.views())
                .param("hasAttachment", boolOrFalse(payload.hasAttachment()))
                .param("category", payload.category())
                .param("bodyText", payload.bodyText())
                .param("bodyHtml", payload.bodyHtml())
                .param("contentHtml", payload.contentHtml())
                .param("contentAssets", toJsonb(payload.contentAssets()))
                .param("contentStats", toJsonb(payload.contentStats()))
                .param("sourceUrl", payload.sourceUrl())
                .param("hash", payload.hash())
                .param("crawledAt", crawledAt)
                .param("deadlineTime", deadlineTime)
                .param("deadlineAt", deadlineAt)
                .query(Long.class)
                .single();
    }

    public long upsertNotice(
            long rawNoticeId,
            RawNoticePayload payload,
            LocalDate noticeDate,
            Integer views,
            LocalDate deadline,
            LocalTime deadlineTime,
            OffsetDateTime deadlineAt,
            OffsetDateTime crawledAt
    ) {
        String sql = """
                INSERT INTO notices (
                    raw_notice_id, article_id, title, is_pinned, author, date,
                    views, category, body_text, body_html, content_html, content_assets, content_stats,
                    source_url, hash, deadline, deadline_time, deadline_at,
                    target, apply_method, crawled_at, published_at
                ) VALUES (
                    :rawNoticeId, :articleId, :title, :isPinned, :author, :date,
                    :views, :category, :bodyText, :bodyHtml, :contentHtml, :contentAssets, :contentStats,
                    :sourceUrl, :hash, :deadline, :deadlineTime, :deadlineAt,
                    :target, :applyMethod, :crawledAt, :publishedAt
                )
                ON CONFLICT (article_id) DO UPDATE SET
                    raw_notice_id = EXCLUDED.raw_notice_id,
                    title = EXCLUDED.title,
                    is_pinned = EXCLUDED.is_pinned,
                    author = EXCLUDED.author,
                    date = EXCLUDED.date,
                    views = EXCLUDED.views,
                    category = EXCLUDED.category,
                    body_text = EXCLUDED.body_text,
                    body_html = EXCLUDED.body_html,
                    content_html = EXCLUDED.content_html,
                    content_assets = EXCLUDED.content_assets,
                    content_stats = EXCLUDED.content_stats,
                    source_url = EXCLUDED.source_url,
                    hash = EXCLUDED.hash,
                    deadline = EXCLUDED.deadline,
                    deadline_time = EXCLUDED.deadline_time,
                    deadline_at = EXCLUDED.deadline_at,
                    target = EXCLUDED.target,
                    apply_method = EXCLUDED.apply_method,
                    crawled_at = EXCLUDED.crawled_at,
                    published_at = EXCLUDED.published_at
                RETURNING id
                """;

        return jdbcClient.sql(sql)
                .param("rawNoticeId", rawNoticeId)
                .param("articleId", payload.articleId())
                .param("title", payload.title())
                .param("isPinned", boolOrFalse(payload.isPinned()))
                .param("author", payload.author())
                .param("date", noticeDate)
                .param("views", views)
                .param("category", payload.category())
                .param("bodyText", payload.bodyText())
                .param("bodyHtml", payload.bodyHtml())
                .param("contentHtml", payload.contentHtml())
                .param("contentAssets", toJsonb(payload.contentAssets()))
                .param("contentStats", toJsonb(payload.contentStats()))
                .param("sourceUrl", payload.sourceUrl())
                .param("hash", payload.hash())
                .param("deadline", deadline)
                .param("deadlineTime", deadlineTime)
                .param("deadlineAt", deadlineAt)
                .param("target", payload.target())
                .param("applyMethod", payload.applyMethod())
                .param("crawledAt", crawledAt)
                .param("publishedAt", crawledAt)
                .query(Long.class)
                .single();
    }

    public void syncAttachments(long noticeId, List<RawAttachmentPayload> attachments) {
        jdbcClient.sql("DELETE FROM notice_attachments WHERE notice_id = :noticeId")
                .param("noticeId", noticeId)
                .update();

        for (RawAttachmentPayload attachment : attachments) {
            if (attachment == null || attachment.fileKey() == null || attachment.fileKey().isBlank()) {
                continue;
            }
            upsertAttachment(noticeId, attachment);
        }
    }

    private void upsertAttachment(long noticeId, RawAttachmentPayload attachment) {
        String sql = """
                INSERT INTO notice_attachments (
                    notice_id, file_key, original_name, ext, file_type, mime_type,
                    file_size, checksum, source_url, local_path, download_ok,
                    extracted_text, extracted_chars, parser, parse_quality, parse_ok, download_cached,
                    preview_pdf_path, preview_pdf_size, preview_pdf_checksum,
                    conversion_status, conversion_engine, conversion_error,
                    r2_url, preview_pdf_r2_url
                ) VALUES (
                    :noticeId, :fileKey, :originalName, :ext, :fileType, :mimeType,
                    :fileSize, :checksum, :sourceUrl, :localPath, :downloadOk,
                    :extractedText, :extractedChars, :parser, :parseQuality, :parseOk, :downloadCached,
                    :previewPdfPath, :previewPdfSize, :previewPdfChecksum,
                    :conversionStatus, :conversionEngine, :conversionError,
                    :r2Url, :previewPdfR2Url
                )
                ON CONFLICT (file_key) DO UPDATE SET
                    notice_id = EXCLUDED.notice_id,
                    original_name = EXCLUDED.original_name,
                    ext = EXCLUDED.ext,
                    file_type = EXCLUDED.file_type,
                    mime_type = EXCLUDED.mime_type,
                    file_size = EXCLUDED.file_size,
                    checksum = EXCLUDED.checksum,
                    source_url = EXCLUDED.source_url,
                    local_path = EXCLUDED.local_path,
                    download_ok = EXCLUDED.download_ok,
                    extracted_text = EXCLUDED.extracted_text,
                    extracted_chars = EXCLUDED.extracted_chars,
                    parser = EXCLUDED.parser,
                    parse_quality = EXCLUDED.parse_quality,
                    parse_ok = EXCLUDED.parse_ok,
                    download_cached = EXCLUDED.download_cached,
                    preview_pdf_path = EXCLUDED.preview_pdf_path,
                    preview_pdf_size = EXCLUDED.preview_pdf_size,
                    preview_pdf_checksum = EXCLUDED.preview_pdf_checksum,
                    conversion_status = EXCLUDED.conversion_status,
                    conversion_engine = EXCLUDED.conversion_engine,
                    conversion_error = EXCLUDED.conversion_error,
                    r2_url = EXCLUDED.r2_url,
                    preview_pdf_r2_url = EXCLUDED.preview_pdf_r2_url
                """;

        String fileKey = requireMaxLength(requiredValue(attachment.fileKey(), "file_key"), "file_key", 500);
        String originalName = requireMaxLength(originalName(attachment), "original_name", 500);
        String ext = requireMaxLength(normalizeExt(attachment.ext()), "ext", 20);
        String mimeType = requireMaxLength(trimToNull(attachment.mimeType()), "mime_type", 100);
        String checksum = requireMaxLength(trimToNull(attachment.checksum()), "checksum", 64);
        String localPath = requireMaxLength(trimToNull(attachment.localPath()), "local_path", 500);
        String parser = requireMaxLength(trimToNull(attachment.parser()), "parser", 50);
        String previewPdfPath = requireMaxLength(
                trimToNull(attachment.previewPdfPath()),
                "preview_pdf_path",
                500
        );
        String previewPdfChecksum = requireMaxLength(
                trimToNull(attachment.previewPdfChecksum()),
                "preview_pdf_checksum",
                64
        );
        String conversionEngine = requireMaxLength(
                trimToNull(attachment.conversionEngine()),
                "conversion_engine",
                50
        );

        jdbcClient.sql(sql)
                .param("noticeId", noticeId)
                .param("fileKey", fileKey)
                .param("originalName", originalName)
                .param("ext", ext)
                .param("fileType", inferFileType(ext))
                .param("mimeType", mimeType)
                .param("fileSize", attachment.fileSize())
                .param("checksum", checksum)
                .param("sourceUrl", attachment.url())
                .param("localPath", localPath)
                .param("downloadOk", boolOrFalse(attachment.downloadOk()))
                .param("extractedText", attachment.extractedText())
                .param("extractedChars", attachment.extractedChars() == null ? 0 : attachment.extractedChars())
                .param("parser", parser)
                .param("parseQuality", normalizeParseQuality(attachment.parseQuality()))
                .param("parseOk", boolOrFalse(attachment.parseOk()))
                .param("downloadCached", boolOrFalse(attachment.downloadCached()))
                .param("previewPdfPath", previewPdfPath)
                .param("previewPdfSize", attachment.previewPdfSize())
                .param("previewPdfChecksum", previewPdfChecksum)
                .param("conversionStatus", normalizeConversionStatus(attachment.conversionStatus()))
                .param("conversionEngine", conversionEngine)
                .param("conversionError", attachment.conversionError())
                .param("r2Url", requireMaxLength(trimToNull(attachment.r2Url()), "r2_url", 500))
                .param("previewPdfR2Url", requireMaxLength(trimToNull(attachment.previewPdfR2Url()), "preview_pdf_r2_url", 500))
                .update();
    }

    public void logImport(String fileName, String status, String message) {
        String sql = """
                INSERT INTO raw_import_log (file_name, status, message)
                VALUES (:fileName, :status, :message)
                """;

        jdbcClient.sql(sql)
                .param("fileName", fileName)
                .param("status", status)
                .param("message", message)
                .update();
    }

    private boolean boolOrFalse(Boolean value) {
        return value != null && value;
    }

    private SqlParameterValue toJsonb(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        return new SqlParameterValue(Types.OTHER, value.toString());
    }

    private String normalizeExt(String ext) {
        if (ext == null || ext.isBlank()) {
            return null;
        }
        return ext.trim().replaceFirst("^\\.", "").toLowerCase(Locale.ROOT);
    }

    private String inferFileType(String ext) {
        if (ext == null) {
            return "other";
        }
        if (IMAGE_EXTENSIONS.contains(ext)) {
            return "image";
        }
        if (DOCUMENT_EXTENSIONS.contains(ext)) {
            return "document";
        }
        if (ARCHIVE_EXTENSIONS.contains(ext)) {
            return "archive";
        }
        return "other";
    }

    private String normalizeParseQuality(String parseQuality) {
        if (parseQuality == null || parseQuality.isBlank()) {
            return "none";
        }
        String normalized = parseQuality.trim().toLowerCase(Locale.ROOT);
        if ("full".equals(normalized) || "preview".equals(normalized) || "none".equals(normalized)) {
            return normalized;
        }
        return "none";
    }

    private String normalizeConversionStatus(String conversionStatus) {
        if (conversionStatus == null || conversionStatus.isBlank()) {
            return "not_applicable";
        }
        String normalized = conversionStatus.trim().toLowerCase(Locale.ROOT);
        if (CONVERSION_STATUSES.contains(normalized)) {
            return normalized;
        }
        return "not_applicable";
    }

    private String originalName(RawAttachmentPayload attachment) {
        if (attachment.name() != null && !attachment.name().isBlank()) {
            return attachment.name().trim();
        }
        return attachment.fileKey().trim();
    }

    private String requiredValue(String value, String fieldName) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException("Attachment " + fieldName + " is required.");
        }
        return trimmed;
    }

    private String requireMaxLength(String value, String fieldName, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(
                    "Attachment " + fieldName + " exceeds " + maxLength + " characters."
            );
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
