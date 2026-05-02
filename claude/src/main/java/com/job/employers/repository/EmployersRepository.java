package com.job.employers.repository;

import com.job.models.Employer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployersRepository extends JpaRepository<Employer, Long> {
    Optional<Employer> findByEmail(String email);
}
