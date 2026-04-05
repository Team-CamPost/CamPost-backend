package com.campost.backend.features.health;

import com.campost.backend.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(Map.of(
                "service", "campost-backend",
                "status", "UP",
                "time", OffsetDateTime.now()
        ));
    }
}
