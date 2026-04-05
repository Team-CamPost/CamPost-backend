package com.campost.backend.features.notices.controller;

import com.campost.backend.common.api.ApiResponse;
import com.campost.backend.features.notices.dto.NoticeDto;
import com.campost.backend.features.notices.service.NoticeQueryService;
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
@RequestMapping("/api/v1/notices")
public class NoticeQueryController {

    private final NoticeQueryService noticeQueryService;

    public NoticeQueryController(NoticeQueryService noticeQueryService) {
        this.noticeQueryService = noticeQueryService;
    }

    @GetMapping
    public ApiResponse<List<NoticeDto>> getNotices(
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return ApiResponse.ok(noticeQueryService.getRecentNotices(limit));
    }
}
