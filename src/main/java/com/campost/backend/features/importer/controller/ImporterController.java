package com.campost.backend.features.importer.controller;

import com.campost.backend.common.api.ApiResponse;
import com.campost.backend.features.importer.service.RawImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/importer")
public class ImporterController {

    private final RawImportService rawImportService;

    public ImporterController(RawImportService rawImportService) {
        this.rawImportService = rawImportService;
    }

    @PostMapping("/run")
    public ApiResponse<Map<String, String>> runImportNow() {
        rawImportService.forceImportAllRawFiles();
        return ApiResponse.ok(Map.of("message", "Importer run triggered (full scan)"));
    }
}
