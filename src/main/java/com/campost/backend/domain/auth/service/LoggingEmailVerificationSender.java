package com.campost.backend.domain.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEmailVerificationSender implements EmailVerificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailVerificationSender.class);

    @Override
    public void send(String email, String code) {
        // Development fallback only. Keep verification data masked even when debug logs are enabled.
        log.debug(
                "Email verification code issued. email={}, code={}",
                maskEmail(email),
                maskCode(code)
        );
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(Math.max(atIndex, 0));
        }

        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private String maskCode(String code) {
        if (code.length() <= 2) {
            return "**";
        }

        return code.substring(0, 2) + "****";
    }
}
