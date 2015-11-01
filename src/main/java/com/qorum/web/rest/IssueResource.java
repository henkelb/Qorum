package com.qorum.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.qorum.domain.Issue;
import com.qorum.repository.IssueRepository;
import com.qorum.web.rest.util.HeaderUtil;
import com.qorum.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Issue.
 */
@RestController
@RequestMapping("/api")
public class IssueResource {

    private final Logger log = LoggerFactory.getLogger(IssueResource.class);

    @Inject
    private IssueRepository issueRepository;

    /**
     * POST  /issues -> Create a new issue.
     */
    @RequestMapping(value = "/issues",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Issue> createIssue(@RequestBody Issue issue) throws URISyntaxException {
        log.debug("REST request to save Issue : {}", issue);
        if (issue.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new issue cannot already have an ID").body(null);
        }
        Issue result = issueRepository.save(issue);
        return ResponseEntity.created(new URI("/api/issues/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("issue", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /issues -> Updates an existing issue.
     */
    @RequestMapping(value = "/issues",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Issue> updateIssue(@RequestBody Issue issue) throws URISyntaxException {
        log.debug("REST request to update Issue : {}", issue);
        if (issue.getId() == null) {
            return createIssue(issue);
        }
        Issue result = issueRepository.save(issue);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("issue", issue.getId().toString()))
            .body(result);
    }

    /**
     * GET  /issues -> get all the issues.
     */
    @RequestMapping(value = "/issues",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Issue>> getAllIssues(Pageable pageable)
        throws URISyntaxException {
        Page<Issue> page = issueRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/issues");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /issues/:id -> get the "id" issue.
     */
    @RequestMapping(value = "/issues/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Issue> getIssue(@PathVariable Long id) {
        log.debug("REST request to get Issue : {}", id);
        return Optional.ofNullable(issueRepository.findOne(id))
            .map(issue -> new ResponseEntity<>(
                issue,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /issues/:id -> delete the "id" issue.
     */
    @RequestMapping(value = "/issues/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteIssue(@PathVariable Long id) {
        log.debug("REST request to delete Issue : {}", id);
        issueRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("issue", id.toString())).build();
    }
}