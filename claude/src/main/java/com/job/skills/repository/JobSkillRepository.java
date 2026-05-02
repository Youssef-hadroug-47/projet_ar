package com.job.skills.repository;

import com.job.models.JobSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {

    List<JobSkill> findByJobId(Long jobId);

}
