package com.optimind.server.module.task.controller;

import java.util.List;
import java.util.UUID;

import com.optimind.server.module.auth.UserAuthenticate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.optimind.server.module.task.dto.ProjectDTO;
import com.optimind.server.module.task.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProjectController {

    private final ProjectService projectService;

    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserAuthenticate ua) {
            return UUID.fromString(ua.getId());
        }
        return UUID.fromString(principal.toString());
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO.ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects(getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<ProjectDTO.ProjectResponse> createProject(
            @RequestBody ProjectDTO.CreateProjectRequest createProjectDto) {
        return ResponseEntity.ok(projectService.createProject(createProjectDto, getCurrentUserId()));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectDTO.ProjectResponse> updateProject(
            @PathVariable UUID projectId,
            @RequestBody ProjectDTO.UpdateProjectRequest updateProjectDto) {
        return ResponseEntity.ok(projectService.updateProject(projectId, updateProjectDto, getCurrentUserId()));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID projectId) {
        projectService.deleteProject(projectId, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
