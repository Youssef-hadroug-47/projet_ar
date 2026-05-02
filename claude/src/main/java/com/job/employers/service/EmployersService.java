package com.job.employers.service;


import com.job.models.Employer;
import com.job.models.Job;
import com.job.models.JobSkill;
import com.job.skills.repository.JobSkillRepository;
import com.job.employers.repository.EmployersRepository;
import com.job.employers.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployersService {

    private final EmployersRepository employerRepository;
    private final JobRepository       jobRepository;
    private final JobSkillRepository  jobSkillRepository;
 
    public EmployersService(EmployersRepository employerRepository,
                            JobRepository jobRepository,
                            JobSkillRepository jobSkillRepository) {
        this.employerRepository = employerRepository;
        this.jobRepository      = jobRepository;
        this.jobSkillRepository = jobSkillRepository;
    }    public Employer getEmployerByEmail(String email) {
        return employerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));
    }

    @Transactional
    public Job createJob(String email, Job request) {
 
        // 1. Resolve employer
        Employer employer = getEmployerByEmail(email);
 
        // 2. Persist the Job first so it gets an id (JobSkill FKs need it)
        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setSalary(request.getSalary());
        job.setEmployer(employer);
        job.setApplications(new ArrayList<>());
        job.setSkills(new ArrayList<>());
        Job savedJob = jobRepository.save(job);
 
        // 3. Resolve / create Skills, then build JobSkill records
        List<JobSkill> jobSkills = new ArrayList<>();
 
        for (JobSkill entry : request.getSkills()) {
 
            String skillName = entry.getSkill().trim();
 
 
            // 3b. Build the JobSkill join record
            JobSkill jobSkill = new JobSkill();
            jobSkill.setJob(savedJob);
            jobSkill.setSkill(skillName);
            jobSkill.setWeight(entry.getWeight());
            jobSkill.setRequiredLevel(entry.getRequiredLevel());
 
            jobSkills.add(jobSkill);
        }
 
        // 4. Persist all JobSkills and attach to the returned Job
        List<JobSkill> savedJobSkills = jobSkillRepository.saveAll(jobSkills);
        savedJob.setSkills(savedJobSkills);
 
        return savedJob;
    }

    public List<Job> getMyJobs(String email) {
        Employer employer = getEmployerByEmail(email);
        return jobRepository.findByEmployerId(employer.getId());
    }

    public Job updateJob(String email, Long jobId, Job updatedJob) {
        Employer employer = getEmployerByEmail(email);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        job.setTitle(updatedJob.getTitle());
        job.setDescription(updatedJob.getDescription());
        job.setSalary(updatedJob.getSalary());
        job.setSkills(updatedJob.getSkills());
        job.setApplications(updatedJob.getApplications());

        return jobRepository.save(job);
    }

    public void deleteJob(String email, Long jobId) {
        Employer employer = getEmployerByEmail(email);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        jobRepository.delete(job);
    }
}
