package com.job.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Employer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String email;
    private String password;

    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL)
    private List<Job> jobs;

    public Long getId(){return this.id;}
    public String getCompanyName(){return this.companyName;}
    public String getEmail(){return this.email;}
    public String getPassword(){return this.password;}
    public List<Job> getJobs(){return this.jobs;}
    public void setId(Long id) {
        this.id = id;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    /**
     * Helper method to maintain bidirectional relationship
     */
    public void addJob(Job job) {
        jobs.add(job);
        job.setEmployer(this);
    }
}
