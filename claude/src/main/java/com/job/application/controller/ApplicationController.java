package com.job.application.controller;

import com.job.application.service.ApplicationService;
import com.job.models.Application;
import com.job.models.Application.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * GET /api/applications/{id}
     * Seeker applies to a job.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<Application> apply(@PathVariable Long id,
                                             Authentication authentication) {
        
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        String seekerEmail = authentication.getName();
        Application application = applicationService.apply(seekerEmail, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(application);
    }

    /**
     * GET /api/applications/me
     * Seeker retrieves their own application history.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<List<Application>> getMyApplications(Authentication authentication) {
        String seekerEmail = authentication.getName();
        List<Application> applications = applicationService.getMyApplications(seekerEmail);
        return ResponseEntity.ok(applications);
    }

    /**
     * GET /api/applications/{jobId}/candidates
     * Employer views applicants for one of their jobs.
     */
    @GetMapping("/{jobId}/candidates")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<Application>> getCandidates(@PathVariable Long jobId,
                                                           Authentication authentication) {
        String employerEmail = authentication.getName();
        List<Application> candidates = applicationService.getCandidates(employerEmail, jobId);
        return ResponseEntity.ok(candidates);
    }

    /**
     * PATCH /api/applications/{id}/status
     * Employer updates an application's status (PENDING / ACCEPTED / REJECTED).
     * Body: { "status": "ACCEPTED" }
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Application> updateStatus(@PathVariable Long id,
                                                    @RequestBody Map<String, String> body,
                                                    Authentication authentication) {
        String rawStatus = body.get("status");
        if (rawStatus == null) {
            return ResponseEntity.badRequest().build();
        }

        Status newStatus;
        try {
            newStatus = Status.valueOf(rawStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        String employerEmail = authentication.getName();
        Application updated = applicationService.updateStatus(employerEmail, id, newStatus);
        return ResponseEntity.ok(updated);
    }
}
