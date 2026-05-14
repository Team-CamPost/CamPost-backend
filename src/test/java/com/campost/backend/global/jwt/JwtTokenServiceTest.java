package com.campost.backend.global.jwt;

import com.campost.backend.global.exception.InvalidTokenException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String JWT_SECRET = "01234567890123456789012345678901";

    private final JwtTokenService jwtTokenService = new JwtTokenService(JWT_SECRET, 3_600_000L);

    @Test
    void parseThrowsInvalidTokenExceptionWhenTokenIsNull() {
        assertThatThrownBy(() -> jwtTokenService.parse(null))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void parseThrowsInvalidTokenExceptionWhenTokenIsEmpty() {
        assertThatThrownBy(() -> jwtTokenService.parse(""))
                .isInstanceOf(InvalidTokenException.class);
    }
}
