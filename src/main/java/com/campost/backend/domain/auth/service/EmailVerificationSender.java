package com.campost.backend.domain.auth.service;

public interface EmailVerificationSender {

    void send(String email, String code);
}
