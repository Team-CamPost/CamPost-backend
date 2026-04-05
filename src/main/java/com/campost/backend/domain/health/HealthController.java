package com.campost.backend.domain.health;

import com.campost.backend.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@Tag(name = "Health", description = "서비스 상태 확인 API")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Operation(summary = "헬스체크", description = "백엔드 서비스 상태를 반환합니다.")
    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(Map.of(
                "service", "campost-backend",
                "status", "UP",
                "time", OffsetDateTime.now()
        ));
    }
}
