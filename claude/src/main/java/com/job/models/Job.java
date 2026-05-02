package com.job.models;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Double salary;

    @ManyToOne
    @JoinColumn(name = "employer_id")
    @JsonIgnore
    private Employer employer;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<JobSkill> skills;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<Application> applications;

    public Long getId(){return this.id;}
    public String getTitle(){return this.title;}
    public String getDescription(){return this.description;}
    public Double getSalary(){return this.salary;}
    public Employer getEmployer(){return this.employer;}
    public List<JobSkill> getSkills(){return this.skills;}
    public List<Application> getApplications(){return this.applications;}
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public void setEmployer(Employer employer) {
        this.employer = employer;
    }

    public void setSkills(List<JobSkill> skills) {
        this.skills = skills;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    // --- Relationship Helper Methods ---

    public void addSkill(JobSkill skill) {
        this.skills.add(skill);
        skill.setJob(this);
    }

    public void addApplication(Application application) {
        this.applications.add(application);
        application.setJob(this);
    }
}
