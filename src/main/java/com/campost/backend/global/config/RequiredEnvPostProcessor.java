package com.campost.backend.global.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequiredEnvPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final List<String> REQUIRED_ENV_VARS = List.of(
            "SPRING_DATASOURCE_URL",
            "SPRING_DATASOURCE_USERNAME",
            "SPRING_DATASOURCE_PASSWORD",
            "APP_CORS_ALLOWED_ORIGINS",
            "JWT_SECRET",
            "JWT_EXPIRY_MS"
    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        List<String> missingOrPlaceholder = new ArrayList<>();
        for (String key : REQUIRED_ENV_VARS) {
            String envKey = Objects.requireNonNull(key);
            if (isMissingOrPlaceholder(environment.getProperty(envKey))) {
                missingOrPlaceholder.add(key);
            }
        }

        if (!missingOrPlaceholder.isEmpty()) {
            throw new IllegalStateException(
                    "Missing required environment variables: "
                            + String.join(", ", missingOrPlaceholder)
                            + ". Set real values in environment/.env before startup."
            );
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isMissingOrPlaceholder(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String trimmed = value.trim();
        return "<REQUIRED>".equalsIgnoreCase(trimmed)
                || "CHANGE_ME".equalsIgnoreCase(trimmed)
                || "REPLACE_ME".equalsIgnoreCase(trimmed);
    }
}
