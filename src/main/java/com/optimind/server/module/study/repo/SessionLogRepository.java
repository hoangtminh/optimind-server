package com.optimind.server.module.study.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optimind.server.module.study.entity.SessionLogEntity;

@Repository
public interface SessionLogRepository extends JpaRepository<SessionLogEntity, UUID> {
    List<SessionLogEntity> findBySession_Id(UUID sessionId);
}
