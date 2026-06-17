package com.max.ai_dev_companion.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.max.ai_dev_companion.dto.CreateProjectRequest;
import com.max.ai_dev_companion.dto.IndexedFileCountResponse;
import com.max.ai_dev_companion.dto.ProjectFileResponse;
import com.max.ai_dev_companion.dto.ProjectIndexResponse;
import com.max.ai_dev_companion.dto.ProjectResponse;
import com.max.ai_dev_companion.service.ProjectIndexService;
import com.max.ai_dev_companion.service.ProjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectIndexService projectIndexService;

    /**
     * Creates a new project.
     *
     * @param request the project creation request containing the name and root path
     * @return the created project data
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProjectResponse createProject(@RequestBody @Valid CreateProjectRequest request) {
        return projectService.createProject(request.name(), request.rootPath());
    }

    /**
     * Returns the list of interesting files for the given project.
     *
     * @param projectId the project identifier
     * @return the list of matching project files
     */
    @GetMapping(value = "/{projectId}/files", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProjectFileResponse> getProjectFiles(@PathVariable UUID projectId) {
        return projectService.listProjectFiles(projectId);
    }

    /**
     * Indexes the project's interesting files in the database.
     *
     * @param projectId the project identifier
     * @return the count of indexed files
     */
    @PostMapping(value = "/{projectId}/files", produces = MediaType.APPLICATION_JSON_VALUE)
    public IndexedFileCountResponse indexProjectFiles(@PathVariable UUID projectId) {
        return projectService.indexProjectFiles(projectId);
    }

    @PostMapping(value = "/{projectId}/index", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProjectIndexResponse indexProject(@PathVariable UUID projectId) {
        return projectIndexService.indexProject(projectId);
    }
}
