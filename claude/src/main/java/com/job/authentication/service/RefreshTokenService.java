package com.job.authentication.service;

import com.job.authentication.model.RefreshToken;
import com.job.authentication.model.User;
import com.job.authentication.repository.RefreshTokenRepository;
import com.job.common.exception.TokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages the full refresh token lifecycle.
 *
 * Rotation strategy: when a client refreshes, the old token is consumed
 * and a new one is issued. If an already-consumed token is presented again
 * (replay attack), ALL tokens for that user are immediately revoked.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final long                   expiryMs;

    public RefreshTokenService(
            RefreshTokenRepository repo,
            @Value("${app.jwt.refresh-token-expiry-ms:604800000}") long expiryMs // 7 days
    ) {
        this.repo     = repo;
        this.expiryMs = expiryMs;
    }

    // ── Issue ──────────────────────────────────────────────────────────────────

    @Transactional
    public RefreshToken create(User user, String deviceInfo) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plusMillis(expiryMs));
        token.setDeviceInfo(deviceInfo);
        return repo.save(token);
    }

    // ── Rotate ─────────────────────────────────────────────────────────────────

    @Transactional
    public RefreshToken rotate(String rawToken) {
        RefreshToken existing = repo.findByToken(rawToken)
                .orElseThrow(() -> new TokenException("Refresh token not found"));

        if (existing.isRevoked()) {
            // Token was already used — possible theft. Nuke all sessions for safety.
            repo.revokeAllByUser(existing.getUser());
            throw new TokenException(
                "Refresh token already used. All sessions revoked for security.");
        }

        if (existing.isExpired()) {
            existing.setRevoked(true);
            repo.save(existing);
            throw new TokenException("Refresh token expired. Please log in again.");
        }

        // Consume the old token
        existing.setRevoked(true);
        repo.save(existing);

        // Issue a fresh one
        return create(existing.getUser(), existing.getDeviceInfo());
    }

    // ── Revoke ─────────────────────────────────────────────────────────────────

    @Transactional
    public void revokeToken(String rawToken) {
        repo.revokeByToken(rawToken);
    }

    @Transactional
    public void revokeAll(User user) {
        repo.revokeAllByUser(user);
    }

    // ── Scheduled cleanup ──────────────────────────────────────────────────────

    /** Runs every night at 03:00 to keep the table clean */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpired() {
        Instant cutoff = Instant.now().minusSeconds(30L * 24 * 3600); // 30 days
        repo.deleteExpiredOrRevoked(cutoff);
    }
}
