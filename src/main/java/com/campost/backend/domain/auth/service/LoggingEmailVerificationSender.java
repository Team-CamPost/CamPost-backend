package com.campost.backend.domain.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEmailVerificationSender implements EmailVerificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailVerificationSender.class);

    @Override
    public void send(String email, String code) {
        log.info("Email verification code issued. email={}, code={}", email, code);
    }
}
