package com.job.authentication.Controller;


import org.springframework.web.bind.annotation.*;

import com.job.authentication.service.AuthentificationService;
import com.job.security.AuthRequest;
import com.job.security.AuthResponse;
import com.job.security.RegisterRequest;

@RestController
@RequestMapping("/auth")
public class AuthentificationController {

    private AuthentificationService authService;

    public AuthentificationController(AuthentificationService authService) {
        this.authService = authService;
    }

    // 📝 POST /auth/register
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return "User registered successfully";
    }

    // 🔐 POST /auth/login
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    // 🔄 POST /auth/refresh
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestParam String refreshToken) {
        return authService.refresh(refreshToken);
    }

    // 🚪 POST /auth/logout
    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String header) {

        String token = header.substring(7); // remove "Bearer "
        authService.logout(token);

        return "Logged out successfully";
    }
}
