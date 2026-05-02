package com.job.search.repository;

import com.job.models.Job;
import com.job.models.Seeker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchRepository extends JpaRepository<Job, Long>,
                                          JpaSpecificationExecutor<Job> {

    /**
     * Find seekers who have ALL of the requested skills (case-insensitive).
     * Uses a HAVING COUNT trick: only seekers whose matching-skill count
     * equals the number of skills asked for are returned.
     */
    @Query("""
           SELECT ss.seeker FROM SeekerSkill ss
           WHERE LOWER(ss.skill) IN :skills
           GROUP BY ss.seeker
           HAVING COUNT(DISTINCT LOWER(ss.skill)) = :skillCount
           """)
    List<Seeker> findSeekersByAllSkills(
            @Param("skills")     List<String> skills,
            @Param("skillCount") long skillCount);
}
