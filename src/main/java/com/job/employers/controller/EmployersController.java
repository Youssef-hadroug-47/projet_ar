package com.job.employers.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public Job createJob(@RequestBody Job job,
                         Authentication authentication) {
        System.out.println("youssef is after you");
        String email = authentication.getName();
        return employersService.createJob(email, job);
    }

    @GetMapping
    public List<Job> getMyJobs(Authentication authentication) {
 
        System.out.println("hadroug is trying to get a list");
        System.out.println(SecurityContextHolder.getContext().getAuthentication());
        String email = authentication.getName();
        return employersService.getMyJobs(email);
    }

    //  PUT /employers/me/jobs/{id}
    @PutMapping("/{id}")
    public Job updateJob(@PathVariable Long id,
                         @RequestBody Job job,
                         Authentication authentication) {
        
        String email = authentication.getName();
        return employersService.updateJob(email, id, job);
    }

    //  DELETE /employers/me/jobs/{id}
    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id,
                          Authentication authentication) {

        String email = authentication.getName();
        employersService.deleteJob(email, id);
    }

}
