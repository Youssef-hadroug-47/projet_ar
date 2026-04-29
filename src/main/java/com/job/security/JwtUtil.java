package com.job.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET = "mysecretkeymysecretkeymysecretkey"; // must be long enough
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    private final long ACCESS_TOKEN_EXP = 1000 * 60 * 60; // 1 hour
    private final long REFRESH_TOKEN_EXP = 1000 * 60 * 60 * 24; // 24 hours

    // 🔐 Generate access token
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXP))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔄 Generate refresh token
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 📤 Extract email
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // 📤 Extract role
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ✅ Validate token
    public boolean validateToken(String token, String email) {
        return extractUsername(token).equals(email) && !isExpired(token);
    }

    // ⏳ Check expiration
    private boolean isExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // 🔍 Parse claims
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
