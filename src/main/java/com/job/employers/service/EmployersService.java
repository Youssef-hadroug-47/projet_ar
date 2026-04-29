package com.job.employers.service;


import com.job.models.Employer;
import com.job.models.Job;
import com.job.employers.repository.EmployersRepository;
import com.job.employers.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployersService {

    private final EmployersRepository employerRepository;
    private final JobRepository jobRepository;

    public EmployersService(EmployersRepository employerRepository,
                            JobRepository jobRepository) {
        this.employerRepository = employerRepository;
        this.jobRepository = jobRepository;
    }

    public Employer getEmployerByEmail(String email) {
        return employerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));
    }

    public Job createJob(String email, Job job) {
        Employer employer = getEmployerByEmail(email);

        job.setEmployer(employer);
        return jobRepository.save(job);
    }

    public List<Job> getMyJobs(String email) {
        Employer employer = getEmployerByEmail(email);
        return jobRepository.findByEmployerId(employer.getId());
    }

    public Job updateJob(String email, Long jobId, Job updatedJob) {
        Employer employer = getEmployerByEmail(email);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // ownership check 🔥
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        job.setTitle(updatedJob.getTitle());
        job.setDescription(updatedJob.getDescription());

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
