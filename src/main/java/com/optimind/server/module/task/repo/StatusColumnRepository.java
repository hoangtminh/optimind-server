package com.optimind.server.module.task.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optimind.server.module.task.entity.StatusColumnEntity;

@Repository
public interface StatusColumnRepository extends JpaRepository<StatusColumnEntity, UUID> {
}
