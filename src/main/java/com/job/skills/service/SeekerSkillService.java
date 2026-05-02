package com.job.skills.service;

import com.job.models.*;
import com.job.skills.repository.SeekerSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SeekerSkillService {

    @Autowired
    private SeekerSkillRepository seekerSkillRepository;


    public List<SeekerSkill> addOrUpdateSkills(Seeker seeker, List<SeekerSkill> inputSkills) {

        List<SeekerSkill> result = new ArrayList<>();

        for (SeekerSkill ss : inputSkills) {

            if (ss.getSkill() == null || ss.getSkill() == null) continue;

            String skill = ss.getSkill();

            SeekerSkill newEntry = new SeekerSkill();
            newEntry.setSeeker(seeker);
            newEntry.setSkill(skill);
            newEntry.setLevel(ss.getLevel());

            result.add(newEntry);
        }

        return seekerSkillRepository.saveAll(result);
    }

    public List<SeekerSkill> getBySeeker(Long seekerId) {
        return seekerSkillRepository.findBySeekerId(seekerId);
    }

    public void delete(Long id) {
        seekerSkillRepository.deleteById(id);
    }
}
