package com.campost.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class StaticFileConfig implements WebMvcConfigurer {

    @Value("${app.files.dir:/data/files}")
    private String filesDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String location = Path.of(filesDir).toAbsolutePath().normalize().toUri().toString();

        registry.addResourceHandler("/files/**")
                .addResourceLocations(location);
    }
}
