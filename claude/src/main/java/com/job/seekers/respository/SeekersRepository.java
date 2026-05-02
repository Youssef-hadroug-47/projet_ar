package com.job.seekers.respository;

import com.job.models.Seeker;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeekersRepository extends JpaRepository<Seeker, Long> {
    Optional<Seeker> findByEmail(String email);
}
