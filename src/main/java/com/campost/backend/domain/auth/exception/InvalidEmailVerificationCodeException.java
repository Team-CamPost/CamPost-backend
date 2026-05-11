package com.campost.backend.domain.auth.exception;

public class InvalidEmailVerificationCodeException extends RuntimeException {

    public InvalidEmailVerificationCodeException() {
        super("이메일 인증번호가 유효하지 않습니다.");
    }
}
