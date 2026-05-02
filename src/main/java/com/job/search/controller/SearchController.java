package com.job.search.controller;

import com.job.models.Job;
import com.job.models.Seeker;
import com.job.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * GET /api/search/jobs
     *
     * Search jobs with any combination of filters (all params optional):
     *   ?min_salary=50000
     *   ?max_salary=100000
     *   ?title=engineer
     *   ?description=remote
     *   ?skill=java
     *
     * All supplied filters are ANDed together.
     * Restricted to SEEKER role.
     */
    @GetMapping("/jobs")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<List<Job>> searchJobs(
            @RequestParam(required = false) Double min_salary,
            @RequestParam(required = false) Double max_salary,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String skill) {

        return ResponseEntity.ok(
                searchService.searchJobs(min_salary, max_salary, title, description, skill));
    }

    /**
     * GET /api/search/seekers?skill=java
     *                        ?skill=java,spring
     *
     * Returns seekers who possess ALL of the specified skills.
     * Multiple skills can be passed as a comma-separated list.
     * Restricted to EMPLOYER role.
     */
    @GetMapping("/seekers")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<Seeker>> searchSeekers(
            @RequestParam(required = false) String skill) {

        return ResponseEntity.ok(searchService.searchSeekers(skill));
    }
}
