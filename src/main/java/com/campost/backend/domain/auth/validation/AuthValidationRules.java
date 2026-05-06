package com.campost.backend.domain.auth.validation;

public final class AuthValidationRules {

    public static final String USERNAME_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$";
    public static final String USERNAME_MESSAGE = "아이디는 영문과 숫자를 모두 포함해 6~20자로 입력해주세요.";
    public static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";
    public static final String PASSWORD_MESSAGE = "비밀번호는 영문과 숫자를 모두 포함해 8자 이상으로 입력해주세요.";

    private AuthValidationRules() {
    }
}
