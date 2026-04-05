package com.campost.backend.domain.collect.query.controller;

import com.campost.backend.global.api.ApiResponse;
import com.campost.backend.domain.collect.query.dto.CrawlJobDto;
import com.campost.backend.domain.collect.query.dto.ParseLogDto;
import com.campost.backend.domain.collect.query.service.CrawlQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@Tag(name = "Collect", description = "크롤링/파싱 수집 상태 조회 API")
@RestController
@RequestMapping({"/api/v1/collect", "/api/v1/crawl"})
public class CrawlQueryController {

    private final CrawlQueryService crawlQueryService;

    public CrawlQueryController(CrawlQueryService crawlQueryService) {
        this.crawlQueryService = crawlQueryService;
    }

        @Operation(summary = "크롤링 Job 목록 조회", description = "최근 크롤링 실행 이력을 조회합니다.")
    @GetMapping("/jobs")
    public ApiResponse<List<CrawlJobDto>> getCrawlJobs(
            @Parameter(description = "조회 개수 (1~100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return ApiResponse.ok(crawlQueryService.getRecentCrawlJobs(limit));
    }

        @Operation(summary = "파싱 로그 조회", description = "최근 첨부파일 파싱 로그를 조회합니다.")
    @GetMapping("/parse-logs")
    public ApiResponse<List<ParseLogDto>> getParseLogs(
            @Parameter(description = "조회 개수 (1~100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return ApiResponse.ok(crawlQueryService.getRecentParseLogs(limit));
    }
}
