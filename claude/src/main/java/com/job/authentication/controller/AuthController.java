package com.job.authentication.controller;

import com.job.authentication.dto.AuthDtos.*;
import com.job.authentication.model.User;
import com.job.authentication.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  PUBLIC (no token needed)                                               │
 * │  POST  /api/auth/register   — create SEEKER or EMPLOYER account         │
 * │  POST  /api/auth/login      — returns access + refresh tokens           │
 * │  POST  /api/auth/refresh    — rotate tokens (old consumed, new issued)  │
 * │  GET   /api/ping            — API health / connection test               │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  PROTECTED (Authorization: Bearer <accessToken> required)               │
 * │  POST  /api/auth/logout     — revoke refresh token(s)                   │
 * │  GET   /api/auth/me         — return current authenticated user         │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ── POST /api/auth/register ────────────────────────────────────────────────

    /**
     * Creates a new account and returns a ready-to-use token pair.
     * No separate login step required after registration.
     *
     * Body: { fullName, email, password, role, companyName? }
     * role must be "SEEKER" or "EMPLOYER".
     */
    @PostMapping("/api/auth/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    // ── POST /api/auth/login ───────────────────────────────────────────────────

    /**
     * Authenticates and returns a fresh token pair.
     * Accounts lock after 5 consecutive failed attempts.
     *
     * Body: { email, password, deviceInfo? }
     */
    @PostMapping("/api/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ── POST /api/auth/refresh ─────────────────────────────────────────────────

    /**
     * Exchanges a valid refresh token for a new token pair.
     * The old refresh token is consumed — it cannot be reused.
     * If a token is reused (replay attack), ALL sessions for that user
     * are immediately revoked.
     *
     * Body: { refreshToken }
     */
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    // ── POST /api/auth/logout ──────────────────────────────────────────────────

    /**
     * Revokes refresh token(s).
     *
     * - Body with refreshToken → single-device logout (only that session)
     * - Empty body or no refreshToken → logout from ALL devices
     *
     * Requires: Authorization: Bearer <accessToken>
     */
    @PostMapping("/api/auth/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> logout(
            @RequestBody(required = false) LogoutRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        boolean loggedOutAll = authService.logout(request, currentUser);
        String msg = loggedOutAll
                ? "Logged out from all devices"
                : "Logged out from this device";
        return ResponseEntity.ok(new MessageResponse(msg));
    }

    // ── GET /api/auth/me ───────────────────────────────────────────────────────

    /**
     * Returns the currently authenticated user's identity and role.
     *
     * Use this to:
     *  1. Check if a stored access token is still valid
     *  2. Bootstrap the frontend on page load (who am I?)
     *  3. Gate role-specific UI before the first API call
     *
     * Returns 200 + user info   → token is valid, user is active
     * Returns 401               → token missing, expired, or tampered
     * Returns 423 Locked        → account has been locked
     *
     * Requires: Authorization: Bearer <accessToken>
     */
    @GetMapping("/api/auth/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> me(@AuthenticationPrincipal User currentUser) {
        if (!currentUser.isAccountNonLocked()) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(new MessageResponse("Account is locked. Contact support."));
        }
        return ResponseEntity.ok(new WhoAmIResponse(currentUser));
    }

    // ── GET /api/ping ──────────────────────────────────────────────────────────

    /**
     * Connection test endpoint — always returns 200.
     *
     * When called WITHOUT a token → { authenticated: false }
     * When called WITH  a token  → { authenticated: true, authenticatedAs: "email" }
     *
     * Useful for:
     *  - Frontend startup checks ("is the API reachable?")
     *  - Postman/curl smoke tests
     *  - Load balancer health probes
     */
    @GetMapping("/api/ping")
    public ResponseEntity<PingResponse> ping(
            @AuthenticationPrincipal User currentUser  // null if no/invalid token
    ) {
        boolean authenticated = currentUser != null;
        String  email         = authenticated ? currentUser.getEmail() : null;
        return ResponseEntity.ok(new PingResponse(authenticated, email));
    }
}
