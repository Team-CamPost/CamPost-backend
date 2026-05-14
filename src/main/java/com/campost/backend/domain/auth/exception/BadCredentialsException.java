package com.campost.backend.domain.auth.exception;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException() {
        super("아이디 또는 비밀번호가 올바르지 않습니다.");
    }
}
