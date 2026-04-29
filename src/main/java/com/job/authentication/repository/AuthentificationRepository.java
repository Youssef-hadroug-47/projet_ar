package com.job.authentication.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployersRepository extends JpaRepository<Employer, Long> {

}
