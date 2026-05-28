package com.campost.backend.global.jwt;

import com.campost.backend.global.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String JWT_SECRET = "01234567890123456789012345678901";
    private static final long ACCESS_TOKEN_EXPIRY_MS = 3_600_000L;
    private static final long REFRESH_TOKEN_EXPIRY_MS = 1_209_600_000L;

    private final JwtTokenService jwtTokenService = new JwtTokenService(
            JWT_SECRET,
            ACCESS_TOKEN_EXPIRY_MS,
            REFRESH_TOKEN_EXPIRY_MS
    );

    @Test
    void generateAccessTokenIncludesUserClaimsAndAccessTokenType() {
        String token = jwtTokenService.generateAccessToken(1L, "campost123", "CamPost User", "USER");

        Claims claims = jwtTokenService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("username", String.class)).isEqualTo("campost123");
        assertThat(claims.get("name", String.class)).isEqualTo("CamPost User");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.get("tokenType", String.class)).isEqualTo(JwtTokenService.ACCESS_TOKEN_TYPE);
        assertThat(tokenLifetimeMs(claims)).isCloseTo(ACCESS_TOKEN_EXPIRY_MS, withinOneSecond());
    }

    @Test
    void generateRefreshTokenIncludesUserClaimsAndRefreshTokenType() {
        String token = jwtTokenService.generateRefreshToken(1L, "campost123", "CamPost User", "USER");

        Claims claims = jwtTokenService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.get("tokenType", String.class)).isEqualTo(JwtTokenService.REFRESH_TOKEN_TYPE);
        assertThat(tokenLifetimeMs(claims)).isCloseTo(REFRESH_TOKEN_EXPIRY_MS, withinOneSecond());
    }

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

    private long tokenLifetimeMs(Claims claims) {
        return claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
    }

    private org.assertj.core.data.Offset<Long> withinOneSecond() {
        return org.assertj.core.data.Offset.offset(1_000L);
    }
}
