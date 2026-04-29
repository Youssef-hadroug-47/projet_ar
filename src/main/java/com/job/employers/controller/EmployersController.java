package com.job.employers.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.job.employers.service.EmployersService;
import com.job.models.Job;

import java.util.List;

@RestController
@RequestMapping("/employers/me/jobs")
public class EmployersController {

    private final EmployersService employersService;

    public EmployersController(EmployersService employersService) {
        this.employersService = employersService;
    }

    // ➕ POST /employers/me/jobs
    @PostMapping
    public Job createJob(@RequestBody Job job,
                         Authentication authentication) {

        String email = authentication.getName();
        return employersService.createJob(email, job);
    }

    // 📄 GET /employers/me/jobs
    @GetMapping
    public List<Job> getMyJobs(Authentication authentication) {

        String email = authentication.getName();
        return employersService.getMyJobs(email);
    }

    // ✏️ PUT /employers/me/jobs/{id}
    @PutMapping("/{id}")
    public Job updateJob(@PathVariable Long id,
                         @RequestBody Job job,
                         Authentication authentication) {

        String email = authentication.getName();
        return employersService.updateJob(email, id, job);
    }

    // ❌ DELETE /employers/me/jobs/{id}
    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id,
                          Authentication authentication) {

        String email = authentication.getName();
        employersService.deleteJob(email, id);
    }

}
