package com.job.application.repository;

import com.job.models.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findBySeekerId(Long seekerId);

    List<Application> findByJobId(Long jobId);

    Optional<Application> findByJobIdAndSeekerId(Long jobId, Long seekerId);
}
