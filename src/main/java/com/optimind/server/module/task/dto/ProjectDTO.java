package com.optimind.server.module.task.dto;

import java.time.Instant;
import java.util.UUID;

public class ProjectDTO {

    public record CreateProjectRequest(
            String name,
            String description
    ) {}

    public record UpdateProjectRequest(
            String name,
            String description
    ) {}

    public record ProjectResponse(
            UUID id,
            String name,
            String description,
            UUID userId,
            Integer taskCount,
            Instant createdAt,
            Instant updatedAt
    ) {}
}