package com.job.skills.repository;

import com.job.models.SeekerSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeekerSkillRepository extends JpaRepository<SeekerSkill, Long> {

    List<SeekerSkill> findBySeekerId(Long seekerId);

}
