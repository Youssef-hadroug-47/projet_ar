package com.job.authentication.repository;

import com.job.authentication.model.RefreshToken;
import com.job.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /** Revoke ALL active tokens for a user — used on logout-all or suspicious reuse */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user AND r.revoked = false")
    void revokeAllByUser(User user);

    /** Revoke a single token by value */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.token = :token")
    void revokeByToken(String token);

    /** Nightly cleanup — removes revoked/expired tokens older than 30 days */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.revoked = true OR r.expiresAt < :cutoff")
    void deleteExpiredOrRevoked(Instant cutoff);
}
