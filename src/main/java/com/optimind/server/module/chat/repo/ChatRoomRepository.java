package com.optimind.server.module.chat.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.optimind.server.module.chat.entity.ChatRoomEnity;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEnity, UUID> {

    @Query("SELECT c FROM ChatRoomEnity c WHERE c.isPublic = false AND c.id IN " +
            "(SELECT cm.chatRoom.id FROM ChatRoomMemberEntity cm GROUP BY cm.chatRoom.id HAVING COUNT(cm.member.id) = 2) AND c.id IN "
            +
            "(SELECT cm.chatRoom.id FROM ChatRoomMemberEntity cm WHERE cm.member.id = :userId1) AND c.id IN " +
            "(SELECT cm.chatRoom.id FROM ChatRoomMemberEntity cm WHERE cm.member.id = :userId2)")
    Optional<ChatRoomEnity> findPrivateChatBetweenUsers(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);
}
