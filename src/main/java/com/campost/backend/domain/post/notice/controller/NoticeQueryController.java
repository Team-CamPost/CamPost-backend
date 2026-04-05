package com.campost.backend.domain.post.notice.controller;

import com.campost.backend.global.api.ApiResponse;
import com.campost.backend.domain.post.notice.dto.NoticeDto;
import com.campost.backend.domain.post.notice.service.NoticeQueryService;
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
@Tag(name = "Post", description = "공지 컨텐츠 조회 API")
@RestController
@RequestMapping({"/api/v1/notices", "/api/v1/post/notices"})
public class NoticeQueryController {

    private final NoticeQueryService noticeQueryService;

    public NoticeQueryController(NoticeQueryService noticeQueryService) {
        this.noticeQueryService = noticeQueryService;
    }

    @Operation(summary = "공지 목록 조회", description = "최신 공지 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<NoticeDto>> getNotices(
            @Parameter(description = "조회 개수 (1~100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return ApiResponse.ok(noticeQueryService.getRecentNotices(limit));
    }
}
