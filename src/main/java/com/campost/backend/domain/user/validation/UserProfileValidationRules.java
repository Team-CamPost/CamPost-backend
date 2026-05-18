package com.campost.backend.domain.user.validation;

public final class UserProfileValidationRules {

    public static final String DEPARTMENT_CODE_PATTERN = "SW|ACE|STAT|INDSEC|AI";
    public static final String DEPARTMENT_CODE_MESSAGE = "지원하지 않는 학과입니다.";
    public static final int MIN_GRADE = 1;
    public static final int MAX_GRADE = 6;
    public static final int MAX_NICKNAME_LENGTH = 50;

    private UserProfileValidationRules() {
    }
}
