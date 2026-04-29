package com.job.models;

import jakarta.persistence.*;

@Entity
public class SeekerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Seeker seeker;

    @ManyToOne
    private Skill skill;

    private int level; // 1–5

    public Long getId(){return this.id;}
    public Seeker getSeeker(){return this.seeker;}
    public Skill getSkill(){return this.skill;}
    public int getLevel(){return this.level;}
    public void setId(Long id) {
        this.id = id;
    }

    public void setSeeker(Seeker seeker) {
        this.seeker = seeker;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}

