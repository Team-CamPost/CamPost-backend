package com.campost.backend.domain.collect.importer.controller;

import com.campost.backend.global.api.ApiResponse;
import com.campost.backend.domain.collect.importer.service.RawImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Collect", description = "크롤링 raw 데이터 적재 API")
@RestController
@RequestMapping({"/api/v1/collect/importer", "/api/v1/importer"})
public class ImporterController {

    private final RawImportService rawImportService;

    public ImporterController(RawImportService rawImportService) {
        this.rawImportService = rawImportService;
    }

    @Operation(summary = "Raw Import 수동 실행", description = "raw JSON 전체 스캔 후 notices 테이블에 적재를 트리거합니다.")
    @PostMapping("/run")
    public ApiResponse<Map<String, String>> runImportNow() {
        rawImportService.forceImportAllRawFiles();
        return ApiResponse.ok(Map.of("message", "Importer run triggered (full scan)"));
    }
}
