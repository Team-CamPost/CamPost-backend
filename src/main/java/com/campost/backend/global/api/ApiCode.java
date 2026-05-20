package com.campost.backend.global.api;

import org.springframework.http.HttpStatus;

public enum ApiCode {
    COMMON200(HttpStatus.OK, "COMMON200", "요청이 성공했습니다."),
    AUTH200_LOGIN(HttpStatus.OK, "AUTH200", "로그인에 성공했습니다."),
    AUTH201(HttpStatus.CREATED, "AUTH201", "회원가입 요청 형식 검증에 성공했습니다."),
    AUTH401_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH401_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다."),
    USER400_SAME_PASSWORD(HttpStatus.BAD_REQUEST, "USER400_SAME_PASSWORD", "새 비밀번호는 현재 비밀번호와 달라야 합니다."),
    AUTH400_EMAIL_VERIFICATION(HttpStatus.BAD_REQUEST, "AUTH400_EMAIL_VERIFICATION", "이메일 인증번호가 유효하지 않습니다."),
    AUTH503_EMAIL_VERIFICATION_SEND(HttpStatus.SERVICE_UNAVAILABLE, "AUTH503_EMAIL_VERIFICATION_SEND", "이메일 인증번호 발송에 실패했습니다."),
    AUTH409(HttpStatus.CONFLICT, "AUTH409", "이미 가입된 이메일입니다."),
    AUTH409_USERNAME(HttpStatus.CONFLICT, "AUTH409_USERNAME", "이미 사용 중인 아이디입니다."),
    COMMON400(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    TOKEN401(HttpStatus.UNAUTHORIZED, "TOKEN401", "액세스 토큰이 만료되었습니다."),
    TOKEN402(HttpStatus.UNAUTHORIZED, "TOKEN402", "유효하지 않은 토큰입니다."),
    AUTH403(HttpStatus.FORBIDDEN, "AUTH403", "접근 권한이 없습니다."),
    COMMON404(HttpStatus.NOT_FOUND, "COMMON404", "리소스를 찾을 수 없습니다."),
    COMMON409(HttpStatus.CONFLICT, "COMMON409", "리소스 충돌이 발생했습니다."),
    SERVER500(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ApiCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
