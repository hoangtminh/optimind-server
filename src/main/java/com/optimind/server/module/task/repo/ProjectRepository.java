package com.optimind.server.module.task.repo;

import com.optimind.server.module.task.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    List<ProjectEntity> findByUser_Id(UUID userId);
    java.util.Optional<ProjectEntity> findByIdAndUser_Id(UUID id, UUID userId);
}