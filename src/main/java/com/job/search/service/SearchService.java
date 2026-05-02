package com.job.search.service;

import com.job.models.Job;
import com.job.models.JobSkill;
import com.job.models.Seeker;
import com.job.search.repository.SearchRepository;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final SearchRepository searchRepository;

    public SearchService(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  SEEKER endpoint  –  search jobs
    //  All parameters are optional and combined with AND.
    //
    //  GET /api/search/jobs?min_salary=&max_salary=&title=&description=&skill=
    // ──────────────────────────────────────────────────────────────────────────

    public List<Job> searchJobs(Double minSalary,
                                Double maxSalary,
                                String title,
                                String description,
                                String skill) {

        Specification<Job> spec = buildJobSpec(minSalary, maxSalary, title, description, skill);
        return searchRepository.findAll(spec);
    }

    private Specification<Job> buildJobSpec(Double minSalary,
                                            Double maxSalary,
                                            String title,
                                            String description,
                                            String skill) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // salary >= minSalary
            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), minSalary));
            }

            // salary <= maxSalary
            if (maxSalary != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), maxSalary));
            }

            // title ILIKE %value%
            if (title != null && !title.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + title.trim().toLowerCase() + "%"));
            }

            // description ILIKE %value%
            if (description != null && !description.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("description")),
                        "%" + description.trim().toLowerCase() + "%"));
            }

            // job must have a JobSkill whose skill name matches (case-insensitive)
            if (skill != null && !skill.isBlank()) {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<JobSkill> skillRoot = sub.from(JobSkill.class);
                sub.select(skillRoot.get("job").get("id"))
                   .where(
                       cb.equal(skillRoot.get("job").get("id"), root.get("id")),
                       cb.like(
                           cb.lower(skillRoot.get("skill")),
                           "%" + skill.trim().toLowerCase() + "%")
                   );
                predicates.add(cb.exists(sub));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  EMPLOYER endpoint  –  search seekers by skill(s)
    //  Accepts a comma-separated list of skills; returns seekers who have ALL.
    //
    //  GET /api/search/seekers?skill=java,spring
    // ──────────────────────────────────────────────────────────────────────────

    public List<Seeker> searchSeekers(String skill) {
        if (skill == null || skill.isBlank()) {
            throw new IllegalArgumentException("skill must not be blank");
        }

        List<String> skills = Arrays.stream(skill.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (skills.isEmpty()) {
            throw new IllegalArgumentException("No valid skills provided");
        }

        return searchRepository.findSeekersByAllSkills(skills, skills.size());
    }
}
