package com.job.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Full Spring Security configuration.
 *
 * Key decisions:
 * - STATELESS sessions — no HttpSession, JWT only
 * - CSRF disabled safely (stateless REST + Bearer tokens)
 * - BCrypt cost 12 — strong against GPU brute-force
 * - Custom JSON 401/403 responses — no HTML error pages
 * - Security headers: HSTS, X-Frame-Options, CSP, no-sniff
 * - @PreAuthorize enabled for method-level role enforcement
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final String[] PUBLIC_POST_PATHS = {
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/refresh"
    };

    private static final String[] PUBLIC_GET_PATHS = {
        "/api/ping",          // connection test — always open
        "/api/jobs",          // public job listing
        "/api/jobs/**"
    };

    private final JwtAuthenticationFilter jwtFilter;
    private final UserDetailsService      userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          UserDetailsService userDetailsService) {
        this.jwtFilter          = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── Stateless — no CSRF needed ─────────────────────────────────
            .csrf(AbstractHttpConfigurer::disable)

            // ── CORS ───────────────────────────────────────────────────────
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── No server-side session ─────────────────────────────────────
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── JSON error responses (no HTML redirects) ───────────────────
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write(
                        "{\"status\":401,\"error\":\"Unauthorized\"," +
                        "\"message\":\"Missing or invalid authentication token\"}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(HttpStatus.FORBIDDEN.value());
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write(
                        "{\"status\":403,\"error\":\"Forbidden\"," +
                        "\"message\":\"You do not have permission to access this resource\"}");
                })
            )

            // ── Route rules ────────────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, PUBLIC_POST_PATHS).permitAll()
                .requestMatchers(HttpMethod.GET,  PUBLIC_GET_PATHS).permitAll()
                .anyRequest().authenticated()
            )

            // ── Security response headers ──────────────────────────────────
            .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31_536_000)
                    .includeSubDomains(true))
                .frameOptions(f -> f.deny())
                .contentTypeOptions(ct -> {})
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
            )

            // ── Wire auth provider and JWT filter ──────────────────────────
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── CORS ──────────────────────────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Replace with your actual frontend origin in production
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://yourfrontend.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ── Auth provider ──────────────────────────────────────────────────────────

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt cost 12 → ~250ms/hash. Never go below 10 in production.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
