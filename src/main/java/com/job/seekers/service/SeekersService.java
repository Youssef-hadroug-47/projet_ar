package com.job.seekers.service;

import com.job.models.Seeker;
import com.job.seekers.respository.SeekersRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import com.job.models.Skill;
import com.job.models.SeekerSkill;
import com.job.seekers.respository.SkillRepository;
import java.util.List;
import java.util.ArrayList;

@Service
public class SeekersService {

    @Autowired
    private SeekersRepository seekersRepository;

    @Autowired
    private SkillRepository skillRepository;

    public Seeker getProfile(String email) {
        return seekersRepository.findByEmail(email).orElse(null);
    }

    public Seeker updateProfile(String email, Seeker updatedSeeker) {
        Seeker seeker = seekersRepository.findByEmail(email).orElse(null);
        if (seeker != null) {
            if (updatedSeeker.getName() != null) seeker.setName(updatedSeeker.getName());
            return seekersRepository.save(seeker);
        }
        return null;
    }

    public Seeker uploadResume(String email, MultipartFile file) throws IOException {
        Seeker seeker = seekersRepository.findByEmail(email).orElse(null);
        if (seeker != null) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                String text = pdfStripper.getText(document);
                seeker.setResumeText(text);
                return seekersRepository.save(seeker);
            }
        }
        return null;
    }

    public Seeker updatePreferences(String email, String preferences) {
        Seeker seeker = seekersRepository.findByEmail(email).orElse(null);
        if (seeker != null) {
            seeker.setPreferences(preferences);
            return seekersRepository.save(seeker);
        }
        return null;
    }

    public Seeker updateSkills(String email, List<SeekerSkill> newSkills) {
        Seeker seeker = seekersRepository.findByEmail(email).orElse(null);
        if (seeker != null) {
            List<SeekerSkill> matchedSkills = new ArrayList<>();
            for (SeekerSkill ss : newSkills) {
                if (ss.getSkill() != null && ss.getSkill().getName() != null) {
                    Skill existingSkill = skillRepository.findByName(ss.getSkill().getName()).orElseGet(() -> {
                        Skill s = new Skill();
                        s.setName(ss.getSkill().getName());
                        return skillRepository.save(s);
                    });
                    SeekerSkill newSs = new SeekerSkill();
                    newSs.setSkill(existingSkill);
                    newSs.setSeeker(seeker);
                    newSs.setLevel(ss.getLevel());
                    matchedSkills.add(newSs);
                }
            }
            if(seeker.getSkills() != null) {
                seeker.getSkills().clear();
                seeker.getSkills().addAll(matchedSkills);
            } else {
                seeker.setSkills(matchedSkills);
            }
            return seekersRepository.save(seeker);
        }
        return null;
    }
}
