package com.job.authentication.dto;

import com.job.authentication.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * All request/response shapes for the auth layer.
 */
public class AuthDtos {

    // ── Requests ───────────────────────────────────────────────────────────────

    public static class RegisterRequest {

        @NotBlank(message = "Full name is required")
        private String fullName;

        @Email(message = "Must be a valid email")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        private User.Role role; // SEEKER or EMPLOYER

        // Only required when role = EMPLOYER
        private String companyName;

        public String getFullName() { return fullName; }
        public void setFullName(String v) { this.fullName = v; }
        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }
        public String getPassword() { return password; }
        public void setPassword(String v) { this.password = v; }
        public User.Role getRole() { return role; }
        public void setRole(User.Role v) { this.role = v; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String v) { this.companyName = v; }
    }

    public static class LoginRequest {

        @Email @NotBlank
        private String email;

        @NotBlank
        private String password;

        private String deviceInfo; // optional, stored with refresh token

        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }
        public String getPassword() { return password; }
        public void setPassword(String v) { this.password = v; }
        public String getDeviceInfo() { return deviceInfo; }
        public void setDeviceInfo(String v) { this.deviceInfo = v; }
    }

    public static class RefreshRequest {

        @NotBlank(message = "Refresh token is required")
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String v) { this.refreshToken = v; }
    }

    public static class LogoutRequest {
        // null = logout from all devices; provided = single-device logout
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String v) { this.refreshToken = v; }
    }

    // ── Responses ──────────────────────────────────────────────────────────────

    public static class AuthResponse {
        private final String accessToken;
        private final String refreshToken;
        private final long   accessTokenExpiresInSeconds;
        private final UserInfo user;

        public AuthResponse(String accessToken, String refreshToken,
                            long expiresIn, UserInfo user) {
            this.accessToken                = accessToken;
            this.refreshToken               = refreshToken;
            this.accessTokenExpiresInSeconds = expiresIn;
            this.user                       = user;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public long getAccessTokenExpiresInSeconds() { return accessTokenExpiresInSeconds; }
        public UserInfo getUser() { return user; }
    }

    public static class UserInfo {
        private final Long   id;
        private final String email;
        private final String fullName;
        private final String role;

        public UserInfo(User user) {
            this.id       = user.getId();
            this.email    = user.getEmail();
            this.fullName = user.getFullName();
            this.role     = user.getRole().name();
        }

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getRole() { return role; }
    }

    /** Response for GET /me */
    public static class WhoAmIResponse {
        private final Long    id;
        private final String  email;
        private final String  fullName;
        private final String  role;
        private final boolean accountNonLocked;

        public WhoAmIResponse(User user) {
            this.id               = user.getId();
            this.email            = user.getEmail();
            this.fullName         = user.getFullName();
            this.role             = user.getRole().name();
            this.accountNonLocked = user.isAccountNonLocked();
        }

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getRole() { return role; }
        public boolean isAccountNonLocked() { return accountNonLocked; }
    }

    public static class MessageResponse {
        private final String message;
        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    /** Response for GET /ping */
    public static class PingResponse {
        private final String  status;
        private final String  message;
        private final long    timestamp;
        private final boolean authenticated;
        private final String  authenticatedAs;

        public PingResponse(boolean authenticated, String email) {
            this.status          = "UP";
            this.message         = "SmartMatch API is running";
            this.timestamp       = System.currentTimeMillis();
            this.authenticated   = authenticated;
            this.authenticatedAs = email;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
        public boolean isAuthenticated() { return authenticated; }
        public String getAuthenticatedAs() { return authenticatedAs; }
    }
}
