package com.campost.backend.domain.collect.importer.controller;

import com.campost.backend.global.api.ApiResponse;
import com.campost.backend.domain.collect.importer.model.RawNoticePayload;
import com.campost.backend.domain.collect.importer.service.RawImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@Tag(name = "Collect", description = "크롤링 raw 데이터 적재 API")
@RestController
@RequestMapping({"/api/v1/collect/importer", "/api/v1/importer"})
public class ImporterController {

    private final RawImportService rawImportService;

    @Value("${app.importer.api-token:}")
    private String importerApiToken;

    public ImporterController(RawImportService rawImportService) {
        this.rawImportService = rawImportService;
    }

    @Operation(summary = "Raw Import 수동 실행", description = "raw JSON 전체 스캔 후 notices 테이블에 적재를 트리거합니다.")
    @PostMapping("/run")
    public ApiResponse<Map<String, String>> runImportNow() {
        rawImportService.forceImportAllRawFiles();
        return ApiResponse.ok(Map.of("message", "Importer run triggered (full scan)"));
    }

    @Operation(summary = "Raw payload import", description = "Pipeline worker가 전송한 raw JSON payload를 즉시 DB에 적재합니다.")
    @PostMapping("/raw")
    public ApiResponse<Map<String, String>> importRawPayload(
            @RequestBody RawNoticePayload payload,
            @RequestHeader(value = "X-Importer-Token", required = false) String importerToken
    ) {
        verifyImporterToken(importerToken);
        String fileName = payload.articleId() == null || payload.articleId().isBlank()
                ? "remote-payload.json"
                : payload.articleId() + ".json";
        rawImportService.importPayload(fileName, payload);
        return ApiResponse.ok(Map.of("message", "Imported", "articleId", payload.articleId()));
    }

    private void verifyImporterToken(String importerToken) {
        if (importerApiToken == null || importerApiToken.isBlank()) {
            return;
        }

        byte[] expected = importerApiToken.getBytes(StandardCharsets.UTF_8);
        byte[] actual = (importerToken == null ? "" : importerToken).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new SecurityException("Invalid importer token");
        }
    }
}
