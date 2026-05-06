package com.campost.backend.domain.auth.controller;

import com.campost.backend.domain.auth.dto.SignupRequest;
import com.campost.backend.domain.auth.dto.SignupResponse;
import com.campost.backend.domain.auth.dto.UsernameAvailabilityResponse;
import com.campost.backend.domain.auth.service.SignupUserService;
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

    public AuthController(SignupUserService signupUserService) {
        this.signupUserService = signupUserService;
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
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$",
                    message = "아이디는 영문과 숫자를 모두 포함해 6~20자로 입력해주세요."
            )
            String username
    ) {
        return ApiResponse.ok(new UsernameAvailabilityResponse(
                username.trim(),
                signupUserService.isUsernameAvailable(username)
        ));
    }
}
