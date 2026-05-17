package com.optimind.server.module.task.repo;

import com.optimind.server.module.task.entity.TaskEntity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    @EntityGraph(attributePaths = { "project", "tag" })
    List<TaskEntity> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<TaskEntity> findByProjectIdAndUser_IdOrderByCreatedAtDesc(UUID projectId, UUID userId);

    Optional<TaskEntity> findByIdAndUserId(UUID taskId, UUID userId);
}