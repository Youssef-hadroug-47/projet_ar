package com.job.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Skill parent;

    @OneToMany(mappedBy = "parent")
    private List<Skill> children;

    public Long getId(){return this.id;}
    public String getName(){return this.name;}
    public Skill getSkill(){return this.parent;}
    public List<Skill> getChildren(){return this.children;}
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(Skill parent) {
        this.parent = parent;
    }

    public void setChildren(List<Skill> children) {
        this.children = children;
    }

    // --- Relationship Helper Method ---

    /**
     * Adds a sub-skill to this skill and automatically sets 
     * this skill as the parent.
     */
    public void addChild(Skill child) {
        this.children.add(child);
        child.setParent(this);
    }
}
