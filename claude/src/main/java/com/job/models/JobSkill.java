package com.job.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
public class JobSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Job job;

    private String skill;

    private double weight; // importance (0–1)

    private int requiredLevel; // optional

    public Long getId(){return this.id;}
    public Job getJob(){return this.job;}
    public String getSkill(){return this.skill;}
    public double getWeight(){return this.weight;}
    public int getRequiredLevel(){return this.requiredLevel;}
    public void setId(Long id) {
        this.id = id;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }
}
