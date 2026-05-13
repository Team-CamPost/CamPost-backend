package com.campost.backend.domain.auth.exception;

public class EmailVerificationSendException extends RuntimeException {

    public EmailVerificationSendException(Throwable cause) {
        super("이메일 인증번호 발송에 실패했습니다.", cause);
    }
}
