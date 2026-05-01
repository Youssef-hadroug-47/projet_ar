package com.job.authentication.Controller;


import org.springframework.web.bind.annotation.*;

import com.job.authentication.service.AuthentificationService;
import com.job.security.AuthRequest;
import com.job.security.AuthResponse;
import com.job.security.JwtUtil;
import com.job.security.RegisterRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthentificationController {

    private AuthentificationService authService;

    public AuthentificationController(AuthentificationService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        System.out.println("youssef");
        authService.register(request);
        return "User registered successfully";
    }


    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        System.out.println("hadroug");
        return authService.login(request);
    }


    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestParam String refreshToken) {
        return authService.refresh(refreshToken);
    }


    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String header) {

        String token = header.substring(7); 
        authService.logout(token);

        return "Logged out successfully";
    }

    @GetMapping("/test")
    public String test(@RequestHeader("Authorization") String header){
        String token = header.substring(7);
        JwtUtil jwtUtil = new JwtUtil();
        return "isExpired :" + (jwtUtil.validateToken(token, "test@test.com") ? "Yes" : "No");
    }
}
