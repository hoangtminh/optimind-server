package com.optimind.server.module.task.service.impl;

import com.optimind.server.module.task.dto.ProjectDTO;
import com.optimind.server.module.task.entity.ProjectEntity;
import com.optimind.server.module.task.repo.ProjectRepository;
import com.optimind.server.module.task.service.ProjectService;
import com.optimind.server.module.user.entity.UserEntity;
import com.optimind.server.module.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public List<ProjectDTO.ProjectResponse> getAllProjects(UUID userId) {
        return projectRepository.findByUser_Id(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProjectDTO.ProjectResponse createProject(ProjectDTO.CreateProjectRequest request, UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProjectEntity project = ProjectEntity.builder()
                .name(request.name())
                .description(request.description())
                .user(user)
                .build();

        project = projectRepository.save(project);
        return toResponse(project);
    }

    @Override
    @Transactional
    public ProjectDTO.ProjectResponse updateProject(UUID projectId, ProjectDTO.UpdateProjectRequest request, UUID userId) {
        ProjectEntity project = projectRepository.findByIdAndUser_Id(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found or access denied"));

        project.setName(request.name());
        project.setDescription(request.description());

        project = projectRepository.save(project);
        return toResponse(project);
    }

    @Override
    @Transactional
    public void deleteProject(UUID projectId, UUID userId) {
        ProjectEntity project = projectRepository.findByIdAndUser_Id(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found or access denied"));
        projectRepository.delete(project);
    }

    private ProjectDTO.ProjectResponse toResponse(ProjectEntity entity) {
        return new ProjectDTO.ProjectResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getUser().getId(),
                entity.getTaskCount() == null ? 0 : entity.getTaskCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}