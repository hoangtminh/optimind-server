package com.optimind.server.module.user.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optimind.server.module.user.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    @org.springframework.data.jpa.repository.Query(
        value = "SELECT u.id as id, u.username as username, u.imageUrl as imageUrl, u.level as level, u.studyTime as studyTime, u.currentStreak as currentStreak, " +
                "COUNT(t.id) as completedTasks " +
                "FROM UserEntity u " +
                "LEFT JOIN TaskEntity t ON t.user.id = u.id AND t.isCompleted = true " +
                "GROUP BY u.id, u.username, u.imageUrl, u.level, u.studyTime, u.currentStreak " +
                "ORDER BY u.studyTime DESC"
    )
    java.util.List<com.optimind.server.module.user.dto.LeaderboardProjection> findLeaderboardTop100(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) + 1 FROM UserEntity u WHERE u.studyTime > :studyTime")
    long calculateRank(Integer studyTime);

    @org.springframework.data.jpa.repository.Query(
        "SELECT u FROM UserEntity u WHERE (:query IS NULL OR :query = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))"
    )
    org.springframework.data.domain.Page<UserEntity> searchUsers(
        @org.springframework.data.repository.query.Param("query") String query, 
        org.springframework.data.domain.Pageable pageable
    );
}