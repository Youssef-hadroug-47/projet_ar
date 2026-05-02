package com.job.employers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.job.employers.service.EmployersService;
import com.job.models.Job;

import java.util.List;

@RestController
@RequestMapping("/api/employers/me/jobs")
public class EmployersController {

    private final EmployersService employersService;

    public EmployersController(EmployersService employersService) {
        this.employersService = employersService;
    }

    
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public Job createJob(@RequestBody Job job,
                         Authentication authentication) {
        String email = authentication.getName();
        return employersService.createJob(email, job);
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public List<Job> getMyJobs(Authentication authentication) {
 
        String email = authentication.getName();
        return employersService.getMyJobs(email);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Job> updateJob(@PathVariable Long id,
                         @RequestBody Job job,
                         Authentication authentication) {
        
        String email = authentication.getName();
        Job new_job = employersService.updateJob(email, id, job);
        return ResponseEntity.ok(new_job); 
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Job> deleteJob(@PathVariable Long id,
                          Authentication authentication) {

        String email = authentication.getName();
        employersService.deleteJob(email, id);
        return ResponseEntity.ok(null);
    }

}
