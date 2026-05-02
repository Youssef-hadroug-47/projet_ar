package com.job.security;

import com.job.authentication.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Handles all JWT operations using jjwt 0.11.x API.
 *
 * Access tokens: short-lived (15 min), carry email + userId + role.
 * The secret is injected from application.properties — never hardcoded.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long accessTokenExpiryMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiry-ms:900000}") long accessTokenExpiryMs
    ) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                "app.jwt.secret must be at least 32 characters long.");
        }
        this.signingKey          = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
    }

    // ── Generate ───────────────────────────────────────────────────────────────

    public String generateAccessToken(User user) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(Map.of(
                    "userId", user.getId(),
                    "role",   user.getRole().name()
                ))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Validate ───────────────────────────────────────────────────────────────

    /**
     * Returns true only if the token is correctly signed and not expired.
     * Logs the failure reason for diagnostics without exposing it externally.
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("JWT bad signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT null/empty: {}", e.getMessage());
        }
        return false;
    }

    // ── Extract ────────────────────────────────────────────────────────────────

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object val = parseClaims(token).get("userId");
        if (val instanceof Integer i) return i.longValue();
        if (val instanceof Long l)    return l;
        return null;
    }

    public String extractRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    public long getAccessTokenExpirySeconds() {
        return accessTokenExpiryMs / 1000;
    }

    // ── Private ────────────────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
