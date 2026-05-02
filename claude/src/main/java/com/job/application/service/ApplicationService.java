package com.job.application.service;

import com.job.application.repository.ApplicationRepository;
import com.job.employers.repository.EmployersRepository;
import com.job.employers.repository.JobRepository;
import com.job.models.Application;
import com.job.models.Application.Status;
import com.job.models.Employer;
import com.job.models.Job;
import com.job.models.Seeker;
import com.job.seekers.respository.SeekersRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final SeekersRepository     seekersRepository;
    private final JobRepository         jobRepository;
    private final EmployersRepository   employersRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              SeekersRepository seekersRepository,
                              JobRepository jobRepository,
                              EmployersRepository employersRepository) {
        this.applicationRepository = applicationRepository;
        this.seekersRepository     = seekersRepository;
        this.jobRepository         = jobRepository;
        this.employersRepository   = employersRepository;
    }

    // POST /applications — seeker applies to a job
    @Transactional
    public Application apply(String seekerEmail, Long jobId) {
        Seeker seeker = seekersRepository.findByEmail(seekerEmail)
                .orElseThrow(() -> new RuntimeException("Seeker profile not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        boolean alreadyApplied = applicationRepository
                .findByJobIdAndSeekerId(jobId, seeker.getId())
                .isPresent();

        if (alreadyApplied) {
            throw new IllegalStateException("You have already applied to this job");
        }

        Application application = new Application();
        application.setSeeker(seeker);
        application.setJob(job);
        application.setStatus(Status.PENDING);

        return applicationRepository.save(application);
    }

    // GET /applications/me — seeker views their own applications
    public List<Application> getMyApplications(String seekerEmail) {
        Seeker seeker = seekersRepository.findByEmail(seekerEmail)
                .orElseThrow(() -> new RuntimeException("Seeker profile not found"));

        return applicationRepository.findBySeekerId(seeker.getId());
    }

    // GET /applications/:jobId/candidates — employer views applicants for their job
    public List<Application> getCandidates(String employerEmail, Long jobId) {
        Employer employer = employersRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new AccessDeniedException("You do not own this job posting");
        }

        return applicationRepository.findByJobId(jobId);
    }

    // PATCH /applications/:id/status — employer updates application status
    @Transactional
    public Application updateStatus(String employerEmail, Long applicationId, Status newStatus) {
        Employer employer = employersRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer profile not found"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Job job = application.getJob();
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new AccessDeniedException("You do not own this job posting");
        }

        application.setStatus(newStatus);
        return applicationRepository.save(application);
    }
}
