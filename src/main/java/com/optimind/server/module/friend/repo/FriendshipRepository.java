package com.optimind.server.module.friend.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.optimind.server.module.friend.entity.FriendshipEntity;

@Repository
public interface FriendshipRepository extends JpaRepository<FriendshipEntity, UUID> {
    @Query("SELECT f FROM FriendshipEntity f WHERE (f.user1.id = :user1Id AND f.user2.id = :user2Id) OR (f.user1.id = :user2Id AND f.user2.id = :user1Id)")
    Optional<FriendshipEntity> findFriendshipBetween(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);

    @Query("SELECT f FROM FriendshipEntity f WHERE f.user1.id = :userId OR f.user2.id = :userId")
    List<FriendshipEntity> findAllByUserId(@Param("userId") UUID userId);
}
