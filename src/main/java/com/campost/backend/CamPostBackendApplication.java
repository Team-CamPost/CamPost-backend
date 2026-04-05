package com.campost.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CamPostBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamPostBackendApplication.class, args);
    }
}
