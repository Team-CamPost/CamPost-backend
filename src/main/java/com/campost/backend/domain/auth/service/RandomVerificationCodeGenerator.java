package com.campost.backend.domain.auth.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RandomVerificationCodeGenerator implements VerificationCodeGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}
