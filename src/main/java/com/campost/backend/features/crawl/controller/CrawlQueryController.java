package com.campost.backend.features.crawl.controller;

import com.campost.backend.common.api.ApiResponse;
import com.campost.backend.features.crawl.dto.CrawlJobDto;
import com.campost.backend.features.crawl.dto.ParseLogDto;
import com.campost.backend.features.crawl.service.CrawlQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/crawl")
public class CrawlQueryController {

    private final CrawlQueryService crawlQueryService;

    public CrawlQueryController(CrawlQueryService crawlQueryService) {
        this.crawlQueryService = crawlQueryService;
    }

    @GetMapping("/jobs")
    public ApiResponse<List<CrawlJobDto>> getCrawlJobs(
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return ApiResponse.ok(crawlQueryService.getRecentCrawlJobs(limit));
    }

    @GetMapping("/parse-logs")
    public ApiResponse<List<ParseLogDto>> getParseLogs(
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return ApiResponse.ok(crawlQueryService.getRecentParseLogs(limit));
    }
}
