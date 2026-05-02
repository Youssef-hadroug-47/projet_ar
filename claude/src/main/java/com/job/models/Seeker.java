package com.job.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Seeker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    @OneToMany(mappedBy = "seeker", cascade = CascadeType.ALL)
    private List<SeekerSkill> skills;

    @OneToMany(mappedBy = "seeker", cascade = CascadeType.ALL)
    private List<Application> applications;

    public Long getId(){return this.id;}
    public String getName(){return this.name;}
    public String getEmail(){return this.email;}
    public String getPassword(){return this.password;}
    public List<SeekerSkill> getSkills(){return this.skills;}
    public List<Application> getApplications(){return this.applications;}
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSkills(List<SeekerSkill> skills) {
        this.skills = skills;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    // --- Relationship Helper Methods ---

    /**
     * Links a skill to this seeker and ensures the SeekerSkill 
     * reference is set correctly.
     */
    public void addSkill(SeekerSkill skill) {
        this.skills.add(skill);
        skill.setSeeker(this);
    }

    /**
     * Links an application to this seeker and ensures the Application 
     * reference is set correctly.
     */
    public void addApplication(Application application) {
        this.applications.add(application);
        application.setSeeker(this);
    }
}

