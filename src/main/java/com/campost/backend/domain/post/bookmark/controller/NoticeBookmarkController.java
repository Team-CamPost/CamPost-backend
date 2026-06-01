package com.campost.backend.domain.post.bookmark.controller;

import com.campost.backend.domain.post.bookmark.dto.NoticeBookmarkResponse;
import com.campost.backend.domain.post.bookmark.service.NoticeBookmarkService;
import com.campost.backend.global.api.ApiResponse;
import com.campost.backend.global.api.ErrorResponse;
import com.campost.backend.global.auth.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "Bookmark", description = "공지 북마크 API")
@RestController
@RequestMapping("/api/v1/notices/{noticeId}/bookmark")
public class NoticeBookmarkController {

    private final NoticeBookmarkService noticeBookmarkService;

    public NoticeBookmarkController(NoticeBookmarkService noticeBookmarkService) {
        this.noticeBookmarkService = noticeBookmarkService;
    }

    @Operation(
            summary = "공지 북마크 저장",
            description = "로그인한 사용자의 북마크 목록에 공지를 저장합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "공지 북마크 저장 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰 누락, 만료 또는 유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "공지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ApiResponse<NoticeBookmarkResponse> bookmark(
            @Parameter(description = "공지 ID")
            @PathVariable @Positive long noticeId,
            @Parameter(hidden = true)
            @LoginUser long userId
    ) {
        return ApiResponse.ok(NoticeBookmarkResponse.from(
                noticeBookmarkService.bookmark(userId, noticeId)
        ));
    }

    @Operation(
            summary = "공지 북마크 해제",
            description = "로그인한 사용자의 북마크 목록에서 공지를 제거합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "공지 북마크 해제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰 누락, 만료 또는 유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "공지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping
    public ApiResponse<NoticeBookmarkResponse> unbookmark(
            @Parameter(description = "공지 ID")
            @PathVariable @Positive long noticeId,
            @Parameter(hidden = true)
            @LoginUser long userId
    ) {
        return ApiResponse.ok(NoticeBookmarkResponse.from(
                noticeBookmarkService.unbookmark(userId, noticeId)
        ));
    }
}
