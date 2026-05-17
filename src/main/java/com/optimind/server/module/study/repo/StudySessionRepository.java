package com.optimind.server.module.study.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optimind.server.module.study.entity.StudySessionEntity;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySessionEntity, UUID> {
    List<StudySessionEntity> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}
