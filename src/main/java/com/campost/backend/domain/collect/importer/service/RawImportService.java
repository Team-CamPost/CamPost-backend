package com.campost.backend.domain.collect.importer.service;

import com.campost.backend.domain.collect.importer.model.RawNoticePayload;
import com.campost.backend.domain.collect.importer.repository.RawImporterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Service
public class RawImportService {

    private static final Logger log = LoggerFactory.getLogger(RawImportService.class);

    private final RawImporterRepository rawImporterRepository;
    private final RawImportTxService rawImportTxService;
    private final ObjectMapper objectMapper;

    @Value("${app.importer.enabled:true}")
    private boolean enabled;

    @Value("${app.importer.raw-store-dir:/data/raw}")
    private String rawStoreDir;

    @Value("${app.importer.batch-size:100}")
    private int batchSize;

    private volatile Instant lastScanTime = Instant.EPOCH;
    private volatile String lastScanPathKey = "";
    private final AtomicBoolean running = new AtomicBoolean(false);

    public RawImportService(
            RawImporterRepository rawImporterRepository,
            RawImportTxService rawImportTxService,
            ObjectMapper objectMapper
    ) {
        this.rawImporterRepository = rawImporterRepository;
        this.rawImportTxService = rawImportTxService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(initialDelayString = "${app.importer.initial-delay-ms:5000}", fixedDelayString = "${app.importer.fixed-delay-ms:30000}")
    public void runScheduledImport() {
        if (!enabled) {
            return;
        }
        importChangedRawFiles(false);
    }

    public void importChangedRawFiles() {
        importChangedRawFiles(false);
    }

    public void forceImportAllRawFiles() {
        importChangedRawFiles(true);
    }

    private void importChangedRawFiles(boolean forceFullScan) {
        if (!running.compareAndSet(false, true)) {
            log.debug("Importer run skipped: another import cycle is already running");
            return;
        }

        try {
            if (forceFullScan) {
                lastScanTime = Instant.EPOCH;
                lastScanPathKey = "";
            }

            Path dir = Path.of(rawStoreDir);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                log.debug("Raw store directory not ready: {}", rawStoreDir);
                return;
            }

            int imported = 0;
            int failed = 0;
            FileCandidate lastContiguousSuccess = null;
            boolean checkpointBlockedByFailure = false;

            try (Stream<Path> stream = Files.list(dir)) {
                List<FileCandidate> targets = stream
                        .filter(path -> path.getFileName().toString().endsWith(".json"))
                        .map(this::toFileCandidate)
                        .filter(Objects::nonNull)
                        .filter(this::isUpdatedAfterLastScan)
                        .sorted(Comparator.comparingLong(FileCandidate::lastModifiedMillis)
                                .thenComparing(FileCandidate::pathKey))
                        .limit(batchSize)
                        .toList();

                for (FileCandidate candidate : targets) {
                    try {
                        importOne(candidate.path());
                        imported++;
                        if (!checkpointBlockedByFailure) {
                            lastContiguousSuccess = candidate;
                        }
                    } catch (Exception ex) {
                        failed++;
                        rawImporterRepository.logImport(candidate.path().getFileName().toString(), "FAILED", ex.getMessage());
                        log.warn("Raw import failed: {} ({})", candidate.path().getFileName(), ex.getMessage());
                        checkpointBlockedByFailure = true;
                    }
                }
            } catch (IOException ex) {
                log.error("Failed to scan raw directory: {}", ex.getMessage());
                return;
            }

            if (lastContiguousSuccess != null) {
                lastScanTime = Instant.ofEpochMilli(lastContiguousSuccess.lastModifiedMillis());
                lastScanPathKey = lastContiguousSuccess.pathKey();
            }

            if (imported > 0 || failed > 0) {
                log.info("Importer run done - imported: {}, failed: {}", imported, failed);
            }
        } finally {
            running.set(false);
        }
    }

    private FileCandidate toFileCandidate(Path path) {
        try {
            long modifiedMillis = Files.getLastModifiedTime(path).toMillis();
            return new FileCandidate(path, modifiedMillis, path.toString());
        } catch (IOException ex) {
            log.debug("Failed to read lastModified for {}: {}", path, ex.getMessage());
            return null;
        }
    }

    private boolean isUpdatedAfterLastScan(FileCandidate candidate) {
        long checkpointMillis = lastScanTime.toEpochMilli();
        if (candidate.lastModifiedMillis() > checkpointMillis) {
            return true;
        }
        if (candidate.lastModifiedMillis() < checkpointMillis) {
            return false;
        }
        return candidate.pathKey().compareTo(lastScanPathKey) > 0;
    }

    private void importOne(Path file) throws IOException {
        RawNoticePayload payload = objectMapper.readValue(file.toFile(), RawNoticePayload.class);

        validatePayload(payload, file);

        OffsetDateTime crawledAt = parseOffsetDateTime(payload.crawledAt());
        LocalDate noticeDate = parseNoticeDate(payload.date());
        Integer views = parseViews(payload.views());
        LocalDate deadline = parseLocalDate(payload.deadline());

        rawImportTxService.importOne(
                file.getFileName().toString(),
                payload,
                crawledAt,
                noticeDate,
                views,
                deadline
        );
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

    private record FileCandidate(Path path, long lastModifiedMillis, String pathKey) {
    }
}
