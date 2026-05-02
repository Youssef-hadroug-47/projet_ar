package com.job.seekers.service;

import com.job.models.Seeker;
import com.job.models.SeekerSkill;
import com.job.seekers.dto.SeekerDtos.*;
import com.job.seekers.respository.SeekersRepository;
import com.job.skills.repository.SeekerSkillRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeekersService {

    private static final List<String> KNOWN_SKILLS = List.of(
        "java", "python", "javascript", "typescript", "c++", "c#", "go", "rust",
        "kotlin", "swift", "ruby", "php", "scala", "r",
        "spring", "spring boot", "django", "flask", "fastapi", "express", "nestjs",
        "react", "angular", "vue", "next.js", "nuxt",
        "sql", "mysql", "postgresql", "mongodb", "redis", "elasticsearch",
        "docker", "kubernetes", "terraform", "aws", "gcp", "azure",
        "git", "ci/cd", "jenkins", "github actions",
        "machine learning", "deep learning", "nlp", "pytorch", "tensorflow",
        "html", "css", "rest", "graphql", "grpc", "kafka", "rabbitmq"
    );

    private final SeekersRepository     seekersRepository;
    private final SeekerSkillRepository seekerSkillRepository;

    public SeekersService(SeekersRepository seekersRepository,
                          SeekerSkillRepository seekerSkillRepository) {
        this.seekersRepository    = seekersRepository;
        this.seekerSkillRepository = seekerSkillRepository;
    }

    public Seeker getByEmail(String email) {
        return seekersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Seeker profile not found"));
    }

    // GET /seekers/me
    public SeekerProfileResponse getProfile(String email) {
        return new SeekerProfileResponse(getByEmail(email));
    }

    // PUT /seekers/me
    @Transactional
    public SeekerProfileResponse updateProfile(String email, UpdateProfileRequest req) {
        Seeker seeker = getByEmail(email);
        if (req.getName() != null && !req.getName().isBlank()) {
            seeker.setName(req.getName());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()
                && !req.getEmail().equals(email)) {
            if (seekersRepository.findByEmail(req.getEmail()).isPresent()) {
                throw new RuntimeException("Email already in use by another account");
            }
            seeker.setEmail(req.getEmail());
        }
        return new SeekerProfileResponse(seekersRepository.save(seeker));
    }

    // POST /seekers/me/resume
    @Transactional
    public ResumeParseResponse uploadResume(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Resume file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new RuntimeException("Only PDF resumes are supported");
        }

        String rawText;
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            rawText = new PDFTextStripper().getText(doc);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse PDF: " + e.getMessage(), e);
        }

        String lower = rawText.toLowerCase();
        List<String> found = KNOWN_SKILLS.stream()
                .filter(skill -> Pattern.compile("\\b" + Pattern.quote(skill) + "\\b")
                        .matcher(lower).find())
                .collect(Collectors.toList());

        Seeker seeker = getByEmail(email);
        seekerSkillRepository.deleteAll(
                seekerSkillRepository.findBySeekerId(seeker.getId()));

        List<SeekerSkill> newSkills = found.stream().map(name -> {
            SeekerSkill ss = new SeekerSkill();
            ss.setSeeker(seeker);
            ss.setSkill(name);
            ss.setLevel(1);
            return ss;
        }).collect(Collectors.toList());
        seekerSkillRepository.saveAll(newSkills);

        return new ResumeParseResponse(rawText, found);
    }

    // PUT /seekers/me/skills
    @Transactional
    public List<SkillResponse> updateSkills(String email, UpdateSkillsRequest req) {
        Seeker seeker = getByEmail(email);
        seekerSkillRepository.deleteAll(
                seekerSkillRepository.findBySeekerId(seeker.getId()));

        if (req.getSkills() == null || req.getSkills().isEmpty()) {
            return List.of();
        }

        List<SeekerSkill> saved = req.getSkills().stream()
                .filter(e -> e.getSkill() != null && !e.getSkill().isBlank())
                .map(e -> {
                    SeekerSkill ss = new SeekerSkill();
                    ss.setSeeker(seeker);
                    ss.setSkill(e.getSkill().trim());
                    ss.setLevel(e.getLevel());
                    return ss;
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(), seekerSkillRepository::saveAll));

        return saved.stream().map(SkillResponse::new).toList();
    }

}
