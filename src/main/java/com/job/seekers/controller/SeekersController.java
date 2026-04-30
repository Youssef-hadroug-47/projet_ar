package com.job.seekers.controller;

import com.job.models.Seeker;
import com.job.models.SeekerSkill;
import com.job.seekers.service.SeekersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seekers")
public class SeekersController {

    @Autowired
    private SeekersService seekersService;

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal()).getUsername();
        } else if (auth != null && auth.getPrincipal() instanceof String) {
            return (String) auth.getPrincipal();
        }
        return "mock@user.com"; // Fallback mock context
    }

    @GetMapping("/me")
    public ResponseEntity<Seeker> getProfile() {
        Seeker seeker = seekersService.getProfile(getCurrentUserEmail());
        if (seeker == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(seeker);
    }

    @PutMapping("/me")
    public ResponseEntity<Seeker> updateProfile(@RequestBody Seeker updatedSeeker) {
        Seeker seeker = seekersService.updateProfile(getCurrentUserEmail(), updatedSeeker);
        if (seeker == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(seeker);
    }

    @PostMapping("/me/resume")
    public ResponseEntity<Seeker> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            Seeker seeker = seekersService.uploadResume(getCurrentUserEmail(), file);
            if (seeker == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(seeker);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/me/skills")
    public ResponseEntity<Seeker> updateSkills(@RequestBody List<SeekerSkill> skills) {
        Seeker seeker = seekersService.updateSkills(getCurrentUserEmail(), skills);
        if (seeker == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(seeker);
    }

    @PutMapping("/me/preferences")
    public ResponseEntity<Seeker> updatePreferences(@RequestBody Map<String, String> payload) {
        String preferences = payload.get("preferences");
        Seeker seeker = seekersService.updatePreferences(getCurrentUserEmail(), preferences);
        if (seeker == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(seeker);
    }
}
