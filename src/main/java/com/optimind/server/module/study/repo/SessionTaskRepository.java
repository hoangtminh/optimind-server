package com.optimind.server.module.study.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optimind.server.module.study.entity.SessionTaskEntity;

@Repository
public interface SessionTaskRepository extends JpaRepository<SessionTaskEntity, Long> {
    List<SessionTaskEntity> findBySession_Id(UUID sessionId);
}
