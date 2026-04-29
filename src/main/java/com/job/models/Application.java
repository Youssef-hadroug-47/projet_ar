package com.job.models;

import jakarta.persistence.*;

@Entity
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Seeker seeker;

    @ManyToOne
    private Job job;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    public Long getId(){return this.id;}
    public Seeker getSeeker(){return this.seeker;}
    public Job getJob(){return this.job;}
    public Status getStatus(){return this.status;}
    public void setId(Long id) {
        this.id = id;
    }

    public void setSeeker(Seeker seeker) {
        this.seeker = seeker;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
