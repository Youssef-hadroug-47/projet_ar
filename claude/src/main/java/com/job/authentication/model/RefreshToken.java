package com.job.authentication.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Persisted refresh token.
 *
 * Storing tokens in the DB (vs. stateless JWT refresh) gives us:
 * - Server-side revocation on logout
 * - Rotation with replay-attack detection
 * - Per-device session visibility
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    /** Optional: label the session (e.g. "Chrome on Windows") */
    private String deviceInfo;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Helpers ────────────────────────────────────────────────────────────────

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    public Instant getCreatedAt() { return createdAt; }
}
