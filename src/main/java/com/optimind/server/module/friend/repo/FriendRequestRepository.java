package com.optimind.server.module.friend.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.optimind.server.module.friend.entity.FriendRequestEntity;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequestEntity, UUID> {
    @Query("SELECT r FROM FriendRequestEntity r WHERE r.sender.id = :senderId AND r.receiver.id = :receiverId")
    Optional<FriendRequestEntity> findPendingRequest(@Param("senderId") UUID senderId,
            @Param("receiverId") UUID receiverId);

    List<FriendRequestEntity> findBySenderId(UUID senderId);

    List<FriendRequestEntity> findByReceiverId(UUID receiverId);
}
