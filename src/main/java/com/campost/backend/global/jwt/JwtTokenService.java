package com.campost.backend.global.jwt;

import com.campost.backend.global.exception.InvalidTokenException;
import com.campost.backend.global.exception.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenService {

    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey secretKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiry-ms}") long accessTokenExpiryMs,
            @Value("${app.jwt.refresh-expiry-ms}") long refreshTokenExpiryMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    public String generateAccessToken(long userId, String username, String name, String role) {
        return generate(userId, username, name, role, ACCESS_TOKEN_TYPE, accessTokenExpiryMs);
    }

    public String generateRefreshToken(long userId) {
        return generate(userId, null, null, null, REFRESH_TOKEN_TYPE, refreshTokenExpiryMs);
    }

    public long accessTokenExpiryMs() {
        return accessTokenExpiryMs;
    }

    public long refreshTokenExpiryMs() {
        return refreshTokenExpiryMs;
    }

    private String generate(
            long userId,
            String username,
            String name,
            String role,
            String tokenType,
            long expiryMs
    ) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("tokenType", tokenType)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(secretKey);

        if (username != null) {
            builder.claim("username", username);
        }

        if (name != null) {
            builder.claim("name", name);
        }

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException("Token has expired.");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid JWT token.");
        }
    }
}
