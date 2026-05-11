package com.campost.backend.domain.auth.exception;

public class UnverifiedEmailException extends RuntimeException {

    public UnverifiedEmailException() {
        super("이메일 인증이 완료되지 않았습니다.");
    }
}
