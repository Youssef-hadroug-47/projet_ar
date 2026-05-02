package com.job.skills.service;

import com.job.models.*;
import com.job.skills.repository.JobSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JobSkillService {

    @Autowired
    private JobSkillRepository jobSkillRepository;


    public List<JobSkill> addSkillsToJob(Job job, List<JobSkill> inputSkills) {

        List<JobSkill> result = new ArrayList<>();

        for (JobSkill js : inputSkills) {

            if (js.getSkill() == null || js.getSkill() == null) continue;

            String skill = js.getSkill();

            JobSkill newEntry = new JobSkill();
            newEntry.setJob(job);
            newEntry.setSkill(skill);
            newEntry.setWeight(js.getWeight());
            newEntry.setRequiredLevel(js.getRequiredLevel());

            result.add(newEntry);
        }

        return jobSkillRepository.saveAll(result);
    }

    public List<JobSkill> getByJob(Long jobId) {
        return jobSkillRepository.findByJobId(jobId);
    }

    public void delete(Long id) {
        jobSkillRepository.deleteById(id);
    }
}
