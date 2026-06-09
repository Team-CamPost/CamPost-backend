package com.campost.backend.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {

    /**
     * true면 토큰이 없거나 유효하지 않을 때 예외를 던진다(필수 인증).
     * false면 비로그인/유효하지 않은 토큰일 때 null을 주입한다(선택적 인증).
     * required=false로 쓰려면 파라미터 타입이 박싱 타입(Long)이어야 한다.
     */
    boolean required() default true;
}
