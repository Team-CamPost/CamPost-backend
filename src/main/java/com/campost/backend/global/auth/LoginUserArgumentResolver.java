package com.campost.backend.global.auth;

import com.campost.backend.global.exception.InvalidTokenException;
import com.campost.backend.global.jwt.JwtTokenService;
import io.jsonwebtoken.Claims;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;

    public LoginUserArgumentResolver(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();

        return parameter.hasParameterAnnotation(LoginUser.class)
                && (Long.class.equals(parameterType) || long.class.equals(parameterType));
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        LoginUser annotation = parameter.getParameterAnnotation(LoginUser.class);
        boolean required = annotation == null || annotation.required();

        String authorization = webRequest.getHeader(AUTHORIZATION_HEADER);

        if (authorization == null || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            if (!required) {
                return null;
            }
            throw new InvalidTokenException("Missing bearer token.");
        }

        try {
            String token = authorization.substring(BEARER_PREFIX.length()).trim();
            Claims claims = jwtTokenService.parse(token);
            String subject = claims.getSubject();

            if (subject == null || subject.isBlank()) {
                throw new InvalidTokenException("Invalid token subject.");
            }

            return Long.parseLong(subject);
        } catch (RuntimeException ex) {
            // 선택적 인증: 토큰이 만료/위조 등으로 유효하지 않으면 비로그인으로 간주한다.
            if (!required) {
                return null;
            }
            if (ex instanceof InvalidTokenException) {
                throw ex;
            }
            throw new InvalidTokenException("Invalid token subject.");
        }
    }
}
