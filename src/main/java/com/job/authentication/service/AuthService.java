package com.job.authentication.service;

import com.job.models.*;
import com.job.authentication.dto.AuthDtos.*;
import com.job.authentication.model.RefreshToken;
import com.job.authentication.model.User;
import com.job.authentication.repository.UserRepository;
import com.job.common.exception.AuthException;
import com.job.employers.repository.EmployersRepository;
import com.job.security.JwtService;
import com.job.seekers.respository.SeekersRepository;

import java.util.ArrayList;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core authentication service.
 *
 * Fixes found in the original code:
 * - Tokens were stored in an in-memory Set — lost on restart, not scalable
 * - Refresh issued role = "USER" regardless of actual role
 * - No brute-force protection
 * - Secret key was hardcoded in JwtUtil
 * - logout() accepted the access token, not the refresh token
 * - No input validation on requests
 */
@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final RefreshTokenService   refreshTokenService;
    private final AuthenticationManager authManager;
    private final EmployersRepository employersRepository;
    private final SeekersRepository seekersRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       AuthenticationManager authManager,
                       EmployersRepository employersRepository,
                       SeekersRepository seekersRepository) {
        this.userRepository      = userRepository;
        this.passwordEncoder     = passwordEncoder;
        this.jwtService          = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authManager         = authManager;
        this.employersRepository = employersRepository;
        this.seekersRepository = seekersRepository;
    }

    // ── Register ───────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AuthException("An account with this email already exists");
        }

        User.Role role = req.getRole() != null ? req.getRole() : User.Role.SEEKER;

        if (role == User.Role.EMPLOYER
                && (req.getCompanyName() == null || req.getCompanyName().isBlank())) {
            throw new AuthException("Company name is required for employer accounts");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(role);
        userRepository.save(user);

        if (role == User.Role.EMPLOYER) {
            Employer employer = new Employer();
            employer.setEmail(req.getEmail());
            employer.setJobs(new ArrayList<>());
            employer.setPassword(req.getPassword());
            employer.setCompanyName(req.getCompanyName());
            employersRepository.save(employer);
        }

        if (role == User.Role.SEEKER) {
            Seeker seeker = new Seeker();
            seeker.setEmail(req.getEmail());    
            seeker.setPassword(req.getPassword());  
            seeker.setName(req.getFullName());
            seeker.setSkills(new ArrayList<>());
            seeker.setApplications(new ArrayList<>());
            seekersRepository.save(seeker);
        }

        return buildAuthResponse(user, null);
    }

    // ── Login ──────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new AuthException("This account has been disabled. Contact support.");
        }
        if (!user.isAccountNonLocked()) {
            throw new AuthException(
                "Account locked after too many failed attempts. Contact support.");
        }

        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (BadCredentialsException e) {
            recordFailedAttempt(user);
            // Generic message — never reveal which field was wrong
            throw new AuthException("Invalid email or password");
        } catch (DisabledException e) {
            throw new AuthException("Account is disabled");
        } catch (LockedException e) {
            throw new AuthException("Account is locked");
        }

        // Successful login — reset counter
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        return buildAuthResponse(user, req.getDeviceInfo());
    }

    // ── Refresh ────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken newToken = refreshTokenService.rotate(req.getRefreshToken());
        User user = newToken.getUser();

        return new AuthResponse(
            jwtService.generateAccessToken(user),
            newToken.getToken(),
            jwtService.getAccessTokenExpirySeconds(),
            new UserInfo(user)
        );
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    @Transactional
    public boolean logout(LogoutRequest req, User currentUser) {
        boolean logoutAll = req == null
                || req.getRefreshToken() == null
                || req.getRefreshToken().isBlank();

        if (logoutAll) {
            refreshTokenService.revokeAll(currentUser);
        } else {
            refreshTokenService.revokeToken(req.getRefreshToken());
        }
        return logoutAll;
    }

    // ── Private ────────────────────────────────────────────────────────────────

    private void recordFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountNonLocked(false);
        }
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user, String deviceInfo) {
        String       accessToken  = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user, deviceInfo);
        return new AuthResponse(
            accessToken,
            refreshToken.getToken(),
            jwtService.getAccessTokenExpirySeconds(),
            new UserInfo(user)
        );
    }
}
