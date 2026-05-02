package com.job.seekers.controller;

import com.job.seekers.dto.SeekerDtos.*;
import com.job.seekers.service.SeekersService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for job-seeker self-service endpoints.
 *
 *  GET  /api/seekers/me              – get own profile
 *  PUT  /api/seekers/me              – update profile
 *  POST /api/seekers/me/resume       – upload & parse PDF resume
 *  PUT  /api/seekers/me/skills       – replace skills list
 *  PUT  /api/seekers/me/preferences  – replace job preferences
 */
@RestController
@RequestMapping("/api/seekers/me")
@PreAuthorize("hasRole('SEEKER')")
public class SeekersController {

    private final SeekersService seekersService;

    public SeekersController(SeekersService seekersService) {
        this.seekersService = seekersService;
    }

    // ── GET /api/seekers/me ───────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<SeekerProfileResponse> getProfile(Authentication auth) {
        return ResponseEntity.ok(seekersService.getProfile(auth.getName()));
    }

    // ── PUT /api/seekers/me ───────────────────────────────────────────────────

    @PutMapping
    public ResponseEntity<SeekerProfileResponse> updateProfile(
            @RequestBody UpdateProfileRequest req,
            Authentication auth) {
        return ResponseEntity.ok(seekersService.updateProfile(auth.getName(), req));
    }

    // ── POST /api/seekers/me/resume ───────────────────────────────────────────

    @PostMapping(value = "/resume", consumes = "multipart/form-data")
    public ResponseEntity<ResumeParseResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        return ResponseEntity.ok(seekersService.uploadResume(auth.getName(), file));
    }

    // ── PUT /api/seekers/me/skills ────────────────────────────────────────────

    @PutMapping("/skills")
    public ResponseEntity<List<SkillResponse>> updateSkills(
            @RequestBody UpdateSkillsRequest req,
            Authentication auth) {
        return ResponseEntity.ok(seekersService.updateSkills(auth.getName(), req));
    }

}
