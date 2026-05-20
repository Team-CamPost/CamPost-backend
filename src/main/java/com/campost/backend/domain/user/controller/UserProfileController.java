package com.campost.backend.domain.user.controller;

import com.campost.backend.domain.user.dto.OnboardingProfileRequest;
import com.campost.backend.domain.user.dto.OnboardingProfileResponse;
import com.campost.backend.domain.user.dto.UserProfileResponse;
import com.campost.backend.domain.user.dto.UserProfileUpdateRequest;
import com.campost.backend.domain.user.service.UserOnboardingProfileService;
import com.campost.backend.domain.user.service.UserProfileQueryService;
import com.campost.backend.domain.user.service.UserProfileUpdateService;
import com.campost.backend.global.api.ApiResponse;
import com.campost.backend.global.api.ErrorResponse;
import com.campost.backend.global.exception.InvalidTokenException;
import com.campost.backend.global.jwt.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserOnboardingProfileService userOnboardingProfileService;
    private final UserProfileQueryService userProfileQueryService;
    private final UserProfileUpdateService userProfileUpdateService;
    private final JwtTokenService jwtTokenService;

    public UserProfileController(
            UserOnboardingProfileService userOnboardingProfileService,
            UserProfileQueryService userProfileQueryService,
            UserProfileUpdateService userProfileUpdateService,
            JwtTokenService jwtTokenService
    ) {
        this.userOnboardingProfileService = userOnboardingProfileService;
        this.userProfileQueryService = userProfileQueryService;
        this.userProfileUpdateService = userProfileUpdateService;
        this.jwtTokenService = jwtTokenService;
    }

    @Operation(
            summary = "내 프로필 정보 수정",
            description = "로그인한 사용자의 닉네임, 학과, 학년 정보를 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 정보 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰 누락, 만료 또는 유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/me/profile")
    public ApiResponse<UserProfileResponse> updateMyProfile(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        long userId = resolveUserId(authorization);

        return ApiResponse.ok(UserProfileResponse.from(
                userProfileUpdateService.updateProfile(userId, request)
        ));
    }

    @Operation(
            summary = "내 정보 조회",
            description = "로그인한 사용자의 아이디, 이메일, 닉네임, 학과, 학년, 프로필 설정 완료 여부를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 정보 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰 누락, 만료 또는 유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(
            @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        long userId = resolveUserId(authorization);

        return ApiResponse.ok(UserProfileResponse.from(
                userProfileQueryService.getProfile(userId)
        ));
    }

    @Operation(
            summary = "첫 로그인 온보딩 프로필 저장",
            description = "로그인한 사용자의 학과, 학년, 닉네임을 저장하고 프로필 설정 완료 상태로 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "온보딩 프로필 저장 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰 누락, 만료 또는 유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/me/onboarding-profile")
    public ApiResponse<OnboardingProfileResponse> saveOnboardingProfile(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody OnboardingProfileRequest request
    ) {
        long userId = resolveUserId(authorization);

        return ApiResponse.ok(OnboardingProfileResponse.from(
                userOnboardingProfileService.saveProfile(userId, request)
        ));
    }

    private long resolveUserId(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new InvalidTokenException("Missing bearer token.");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        Claims claims = jwtTokenService.parse(token);

        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException ex) {
            throw new InvalidTokenException("Invalid token subject.");
        }
    }
}
