package com.campost.backend.domain.auth.controller;

import com.campost.backend.domain.auth.dto.EmailVerificationCodeRequest;
import com.campost.backend.domain.auth.dto.EmailVerificationCodeResponse;
import com.campost.backend.domain.auth.dto.EmailVerificationCheckRequest;
import com.campost.backend.domain.auth.dto.EmailVerificationCheckResponse;
import com.campost.backend.domain.auth.dto.LoginRequest;
import com.campost.backend.domain.auth.dto.LoginResponse;
import com.campost.backend.domain.auth.dto.SignupRequest;
import com.campost.backend.domain.auth.dto.SignupResponse;
import com.campost.backend.domain.auth.dto.TokenRefreshRequest;
import com.campost.backend.domain.auth.dto.TokenRefreshResponse;
import com.campost.backend.domain.auth.dto.UsernameAvailabilityResponse;
import com.campost.backend.domain.auth.service.EmailVerificationService;
import com.campost.backend.domain.auth.service.LoginService;
import com.campost.backend.domain.auth.service.SignupUserService;
import com.campost.backend.domain.auth.service.TokenRefreshService;
import com.campost.backend.domain.auth.validation.AuthValidationRules;
import com.campost.backend.global.api.ApiCode;
import com.campost.backend.global.api.ApiResponse;
import com.campost.backend.global.api.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final SignupUserService signupUserService;
    private final EmailVerificationService emailVerificationService;
    private final LoginService loginService;
    private final TokenRefreshService tokenRefreshService;

    public AuthController(
            SignupUserService signupUserService,
            EmailVerificationService emailVerificationService,
            LoginService loginService,
            TokenRefreshService tokenRefreshService
    ) {
        this.signupUserService = signupUserService;
        this.emailVerificationService = emailVerificationService;
        this.loginService = loginService;
        this.tokenRefreshService = tokenRefreshService;
    }

    @Operation(
            summary = "로그인",
            description = "아이디와 비밀번호로 로그인하고 JWT 액세스 토큰을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "아이디 또는 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(ApiCode.AUTH200_LOGIN, loginService.login(request));
    }

    @Operation(
            summary = "Access Token 재발급",
            description = "Refresh Token을 검증하고 최신 사용자 정보로 새 Access Token을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Access Token 재발급 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "만료되었거나 유효하지 않은 Refresh Token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/token/refresh")
    public ApiResponse<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        return ApiResponse.ok(ApiCode.AUTH200_TOKEN_REFRESH, tokenRefreshService.refresh(request));
    }

    @Operation(
            summary = "회원가입",
            description = "아이디, 이메일, 비밀번호를 검증하고 회원가입 응답 형식을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 요청 형식 검증 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이메일 또는 아이디 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(ApiCode.AUTH201, SignupResponse.from(signupUserService.saveUser(request))));
    }

    @Operation(
            summary = "아이디 중복 확인",
            description = "회원가입 아이디가 사용 가능한지 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "아이디 사용 가능 여부 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "아이디 형식 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/check-username")
    public ApiResponse<UsernameAvailabilityResponse> checkUsername(
            @RequestParam
            @Pattern(
                    regexp = AuthValidationRules.USERNAME_PATTERN,
                    message = AuthValidationRules.USERNAME_MESSAGE
            )
            String username
    ) {
        return ApiResponse.ok(new UsernameAvailabilityResponse(
                username.trim(),
                signupUserService.isUsernameAvailable(username)
        ));
    }

    @Operation(
            summary = "이메일 인증번호 전송",
            description = "회원가입에 사용할 이메일로 인증번호를 전송합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이메일 인증번호 전송 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 가입된 이메일",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/email/verification-code")
    public ApiResponse<EmailVerificationCodeResponse> sendEmailVerificationCode(
            @Valid @RequestBody EmailVerificationCodeRequest request
    ) {
        return ApiResponse.ok(emailVerificationService.sendCode(request));
    }

    @Operation(
            summary = "이메일 인증번호 확인",
            description = "회원가입에 사용할 이메일 인증번호가 유효한지 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이메일 인증번호 확인 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패 또는 유효하지 않은 인증번호",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/email/verification-code/check")
    public ApiResponse<EmailVerificationCheckResponse> checkEmailVerificationCode(
            @Valid @RequestBody EmailVerificationCheckRequest request
    ) {
        return ApiResponse.ok(emailVerificationService.verifyCode(request));
    }
}
