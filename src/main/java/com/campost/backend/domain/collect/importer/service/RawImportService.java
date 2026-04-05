package com.campost.backend.domain.collect.importer.service;

import com.campost.backend.domain.collect.importer.model.RawNoticePayload;
import com.campost.backend.domain.collect.importer.repository.RawImporterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class RawImportService {

    private static final Logger log = LoggerFactory.getLogger(RawImportService.class);

    private final RawImporterRepository rawImporterRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.importer.enabled:true}")
    private boolean enabled;

    @Value("${app.importer.raw-store-dir:/data/raw}")
    private String rawStoreDir;

    @Value("${app.importer.batch-size:100}")
    private int batchSize;

    private volatile Instant lastScanTime = Instant.EPOCH;

    public RawImportService(RawImporterRepository rawImporterRepository, ObjectMapper objectMapper) {
        this.rawImporterRepository = rawImporterRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(initialDelayString = "${app.importer.initial-delay-ms:5000}", fixedDelayString = "${app.importer.fixed-delay-ms:30000}")
    public void runScheduledImport() {
        if (!enabled) {
            return;
        }
        importChangedRawFiles();
    }

    @Transactional
    public void importChangedRawFiles() {
        Path dir = Path.of(rawStoreDir);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            log.debug("Raw store directory not ready: {}", rawStoreDir);
            return;
        }

        Instant scanStartedAt = Instant.now();
        int imported = 0;
        int failed = 0;

        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> targets = stream
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(this::isUpdatedAfterLastScan)
                    .sorted(Comparator.comparing(Path::toString))
                    .limit(batchSize)
                    .toList();

            for (Path file : targets) {
                try {
                    importOne(file);
                    imported++;
                } catch (Exception ex) {
                    failed++;
                    rawImporterRepository.logImport(file.getFileName().toString(), "FAILED", ex.getMessage());
                    log.warn("Raw import failed: {} ({})", file.getFileName(), ex.getMessage());
                }
            }
        } catch (IOException ex) {
            log.error("Failed to scan raw directory: {}", ex.getMessage());
            return;
        }

        lastScanTime = scanStartedAt;

        if (imported > 0 || failed > 0) {
            log.info("Importer run done - imported: {}, failed: {}", imported, failed);
        }
    }

    @Transactional
    public void forceImportAllRawFiles() {
        lastScanTime = Instant.EPOCH;
        importChangedRawFiles();
    }

    private boolean isUpdatedAfterLastScan(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant().isAfter(lastScanTime);
        } catch (IOException ex) {
            return false;
        }
    }

    private void importOne(Path file) throws IOException {
        RawNoticePayload payload = objectMapper.readValue(file.toFile(), RawNoticePayload.class);

        validatePayload(payload, file);

        OffsetDateTime crawledAt = parseOffsetDateTime(payload.crawledAt());
        LocalDate noticeDate = parseNoticeDate(payload.date());
        Integer views = parseViews(payload.views());
        LocalDate deadline = parseLocalDate(payload.deadline());

        long rawNoticeId = rawImporterRepository.upsertRawNotice(payload, crawledAt);
        rawImporterRepository.upsertNotice(rawNoticeId, payload, noticeDate, views, deadline, crawledAt);
        rawImporterRepository.logImport(file.getFileName().toString(), "SUCCESS", "Imported to raw_notices/notices");
    }

    private void validatePayload(RawNoticePayload payload, Path file) {
        if (payload.articleId() == null || payload.articleId().isBlank()) {
            throw new IllegalArgumentException("article_id is required: " + file.getFileName());
        }
        if (payload.title() == null || payload.title().isBlank()) {
            throw new IllegalArgumentException("title is required: " + file.getFileName());
        }
        if (payload.hash() == null || payload.hash().isBlank()) {
            throw new IllegalArgumentException("hash is required: " + file.getFileName());
        }
        if (payload.sourceId() == null) {
            throw new IllegalArgumentException("source_id is required: " + file.getFileName());
        }
    }

    private OffsetDateTime parseOffsetDateTime(String value) {
        if (value == null || value.isBlank()) {
            return OffsetDateTime.now(ZoneOffset.UTC);
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            return OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    private LocalDate parseNoticeDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy.MM.dd"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(value.trim(), formatter);
            } catch (DateTimeParseException ignored) {
                // try next formatter
            }
        }
        return null;
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private Integer parseViews(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        String digitsOnly = value.replaceAll("[^0-9]", "");
        if (digitsOnly.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(digitsOnly);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
