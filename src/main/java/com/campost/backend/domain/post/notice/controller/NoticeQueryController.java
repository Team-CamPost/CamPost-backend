package com.campost.backend.domain.post.notice.controller;

import com.campost.backend.global.api.ApiResponse;
import com.campost.backend.global.auth.LoginUser;
import com.campost.backend.domain.post.notice.dto.NoticeDetailDto;
import com.campost.backend.domain.post.notice.dto.NoticeDto;
import com.campost.backend.domain.post.notice.service.NoticeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(summary = "공지 목록 조회", description = "최신 및 마감 임박 공지 목록을 학과별로 조회합니다. 로그인 상태면 isBookmarked가 채워집니다.")
    @GetMapping
    public ApiResponse<List<NoticeDto>> getNotices(
            @Parameter(description = "조회 개수 (1~100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,

            @Parameter(description = "학과 코드 필터링 (예: cse)", required = false)
            @RequestParam(required = false) String deptCode,

            @Parameter(description = "정렬 방식 (recent: 최신순, deadline: 마감임박순)", required = false)
            @RequestParam(defaultValue = "recent") String sortBy,

            @Parameter(hidden = true)
            @LoginUser(required = false) Long userId
    ) {
        return ApiResponse.ok(noticeQueryService.getNotices(limit, deptCode, sortBy, userId));
    }

    @Operation(
            summary = "내 북마크 공지 목록 조회",
            description = "로그인한 사용자가 북마크한 공지 목록을 최근 북마크순으로 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/bookmarked")
    public ApiResponse<List<NoticeDto>> getBookmarkedNotices(
            @Parameter(hidden = true)
            @LoginUser long userId,

            @Parameter(description = "조회 개수 (1~100)")
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    ) {
        return ApiResponse.ok(noticeQueryService.getBookmarkedNotices(userId, limit));
    }

    @Operation(
            summary = "공지 상세 조회",
            description = "공지 1건의 상세 정보를 조회합니다. 로그인 상태면 isBookmarked가 채워집니다."
    )
    @GetMapping("/{noticeId}")
    public ApiResponse<NoticeDetailDto> getNoticeDetail(
            @Parameter(description = "공지 ID")
            @PathVariable @Positive long noticeId,

            @Parameter(hidden = true)
            @LoginUser(required = false) Long userId
    ) {
        return ApiResponse.ok(noticeQueryService.getNoticeDetail(noticeId, userId));
    }
}
