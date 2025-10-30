package com.learningplatform.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT validation utility for API Gateway.
 * Validates JWT tokens and extracts user information.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:your-256-bit-secret-change-this-in-production-make-it-at-least-32-characters-long}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public String extractUserId(Claims claims) {
        return claims.getSubject();
    }

    public String extractUsername(Claims claims) {
        return claims.get("username", String.class);
    }
}
