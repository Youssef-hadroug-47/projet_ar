package com.job.authentication.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.job.employers.repository.EmployersRepository;
import com.job.models.Employer;
import com.job.models.Seeker;
import com.job.security.AuthRequest;
import com.job.security.AuthResponse;
import com.job.security.JwtUtil;
import com.job.security.RegisterRequest;
import com.job.seekers.respository.*;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthentificationService {

    private  SeekersRepository seekerRepository;
    private  EmployersRepository employerRepository;
    private  PasswordEncoder passwordEncoder;
    private  JwtUtil jwtUtil;

    private final Set<String> invalidatedTokens = new HashSet<>();

    public AuthentificationService(SeekersRepository seekerRepository,
                       EmployersRepository employerRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.seekerRepository = seekerRepository;
        this.employerRepository = employerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest request) {

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        if ("SEEKER".equalsIgnoreCase(request.getRole())) {

            Seeker seeker = new Seeker();
            seeker.setEmail(request.getEmail());
            seeker.setPassword(encodedPassword);
            seeker.setName(request.getName());

            seekerRepository.save(seeker);

        } else if ("EMPLOYER".equalsIgnoreCase(request.getRole())) {

            Employer employer = new Employer();
            employer.setEmail(request.getEmail());
            employer.setPassword(encodedPassword);
            employer.setCompanyName(request.getCompanyName());

            employerRepository.save(employer);

        } else {
            throw new RuntimeException("Invalid role");
        }
    }

    public AuthResponse login(AuthRequest request) {

        Seeker seeker = seekerRepository.findByEmail(request.getEmail()).orElse(null);
        if (seeker != null &&
            passwordEncoder.matches(request.getPassword(), seeker.getPassword())) {

            String accessToken = jwtUtil.generateToken(seeker.getEmail(), "SEEKER");
            String refreshToken = jwtUtil.generateRefreshToken(seeker.getEmail());

            return new AuthResponse(accessToken, refreshToken);
        }

        Employer employer = employerRepository.findByEmail(request.getEmail()).orElse(null);
        if (employer != null &&
            passwordEncoder.matches(request.getPassword(), employer.getPassword())) {

            String accessToken = jwtUtil.generateToken(employer.getEmail(), "EMPLOYER");
            String refreshToken = jwtUtil.generateRefreshToken(employer.getEmail());

            return new AuthResponse(accessToken, refreshToken);
        }

        throw new RuntimeException("Invalid credentials");
    }

    public AuthResponse refresh(String refreshToken) {

        if (invalidatedTokens.contains(refreshToken)) {
            throw new RuntimeException("Token invalidated");
        }

        String email = jwtUtil.extractUsername(refreshToken);

        String newAccessToken = jwtUtil.generateToken(email, "USER");

        return new AuthResponse(newAccessToken, refreshToken);
    }

    public void logout(String token) {
        invalidatedTokens.add(token);
    }
}
