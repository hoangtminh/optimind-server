package com.optimind.server.module.task.service;

import java.util.List;
import java.util.UUID;
import com.optimind.server.module.task.dto.ProjectDTO;

public interface ProjectService {
    List<ProjectDTO.ProjectResponse> getAllProjects(UUID userId);

    ProjectDTO.ProjectResponse createProject(ProjectDTO.CreateProjectRequest request, UUID userId);
    
    ProjectDTO.ProjectResponse updateProject(UUID projectId, ProjectDTO.UpdateProjectRequest request, UUID userId);
    
    void deleteProject(UUID projectId, UUID userId);
}