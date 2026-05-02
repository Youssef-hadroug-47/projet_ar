package com.job.seekers.dto;

import com.job.models.Seeker;
import com.job.models.SeekerSkill;

import java.util.List;

/**
 * Request/response shapes for the /seekers/me family of endpoints.
 */
public class SeekerDtos {

    // ── Requests ──────────────────────────────────────────────────────────────

    /** PUT /seekers/me — update basic profile fields */
    public static class UpdateProfileRequest {
        private String name;
        private String email;

        public String getName()  { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    /** PUT /seekers/me/skills — replace the full skills list */
    public static class UpdateSkillsRequest {
        private List<SkillEntry> skills;

        public List<SkillEntry> getSkills() { return skills; }
        public void setSkills(List<SkillEntry> skills) { this.skills = skills; }

        public static class SkillEntry {
            private String skill;
            private int level; // 1–5

            public String getSkill()  { return skill; }
            public void setSkill(String skill) { this.skill = skill; }
            public int getLevel()     { return level; }
            public void setLevel(int level) { this.level = level; }
        }
    }

    /** PUT /seekers/me/preferences — replace job-preference list */
    public static class UpdatePreferencesRequest {
        /** Free-form tags: "remote", "full-time", "Berlin", "fintech", … */
        private List<String> preferences;

        public List<String> getPreferences() { return preferences; }
        public void setPreferences(List<String> preferences) { this.preferences = preferences; }
    }

    // ── Responses ─────────────────────────────────────────────────────────────

    /** Returned for GET and PUT /seekers/me */
    public static class SeekerProfileResponse {
        private final Long   id;
        private final String name;
        private final String email;
        private final List<SkillResponse> skills;

        public SeekerProfileResponse(Seeker seeker) {
            this.id    = seeker.getId();
            this.name  = seeker.getName();
            this.email = seeker.getEmail();


            this.skills = seeker.getSkills() == null
                ? List.of()
                : seeker.getSkills().stream().map(SkillResponse::new).toList();
        }

        public Long   getId()             { return id; }
        public String getName()           { return name; }
        public String getEmail()          { return email; }
        public List<SkillResponse> getSkills()  { return skills; }
    }

    public static class SkillResponse {
        private final Long   id;
        private final String skill;
        private final int    level;

        public SkillResponse(SeekerSkill ss) {
            this.id    = ss.getId();
            this.skill = ss.getSkill();
            this.level = ss.getLevel();
        }

        public Long   getId()    { return id; }
        public String getSkill() { return skill; }
        public int    getLevel() { return level; }
    }

    /** Returned after POST /seekers/me/resume */
    public static class ResumeParseResponse {
        private final String rawText;
        private final List<String> extractedSkills;

        public ResumeParseResponse(String rawText, List<String> extractedSkills) {
            this.rawText         = rawText;
            this.extractedSkills = extractedSkills;
        }

        public String       getRawText()        { return rawText; }
        public List<String> getExtractedSkills() { return extractedSkills; }
    }
}
