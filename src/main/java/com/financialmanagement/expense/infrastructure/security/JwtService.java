package com.financialmanagement.expense.infrastructure.security;

import com.financialmanagement.expense.application.port.out.JwtAccessTokenPort;
import com.financialmanagement.expense.domain.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService implements JwtAccessTokenPort {

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds) {
        byte[] bytes = decodeOrDeriveKeyBytes(secret);
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationSeconds = expirationSeconds;
    }

    private static byte[] decodeOrDeriveKeyBytes(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret must be non-blank");
        }

        String trimmed = secret.trim();

        // Prefer explicit Base64/Base64URL decoding when provided.
        try {
            return Decoders.BASE64.decode(trimmed);
        } catch (RuntimeException ignored) {
            // fall through
        }
        try {
            return Decoders.BASE64URL.decode(trimmed);
        } catch (RuntimeException ignored) {
            // fall through
        }

        // Treat as raw string secret and derive a fixed-size key (32 bytes) for HS256.
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(trimmed.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public String createToken(UUID userId, String email, UserRole role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("roles", List.of(role.name()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    @Override
    public String createAccessToken(UUID userId, String email, UserRole role) {
        return createToken(userId, email, role);
    }

    @Override
    public long getAccessTokenTtlSeconds() {
        return expirationSeconds;
    }
}
