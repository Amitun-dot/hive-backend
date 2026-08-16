package com.hive.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Creates the signing key from the configured JWT secret.
     *
     * Supports:
     * 1. Base64 encoded secrets
     * 2. Plain text secrets (minimum 32 bytes)
     */
    private SecretKey getSigningKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }

        String trimmedSecret = secret.trim();

        // First try standard Base64.
        try {
            byte[] keyBytes = Decoders.BASE64.decode(trimmedSecret);

            if (keyBytes.length >= 32) {
                return Keys.hmacShaKeyFor(keyBytes);
            }
        } catch (Exception e) {
            log.debug("JWT secret is not Base64. Using it as a raw secret.");
        }

        // Fallback: use the secret directly as UTF-8 bytes.
        byte[] keyBytes = trimmedSecret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes long"
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String userId, String email, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);

            Date expirationDate = claims.getExpiration();

            return expirationDate != null
                    && !expirationDate.before(new Date());

        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public long getExpiration() {
        return expiration;
    }
}