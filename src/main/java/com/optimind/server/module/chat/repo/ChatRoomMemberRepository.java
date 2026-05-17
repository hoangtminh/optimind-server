package com.optimind.server.module.chat.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.optimind.server.module.chat.entity.ChatRoomMemberEntity;
import com.optimind.server.module.chat.entity.ChatRoomMemberId;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMemberEntity, ChatRoomMemberId> {
    // Sử dụng EntityGraph để lấy thông tin ChatRoom cùng lúc, tránh N+1
    @EntityGraph(attributePaths = { "chatRoom" })
    List<ChatRoomMemberEntity> findByMember_IdOrderByChatRoom_LastActiveDesc(UUID userId);

    // Xóa trực tiếp bằng Query để đạt hiệu năng cao
    @Modifying
    @Query("DELETE FROM ChatRoomMemberEntity m WHERE m.chatRoom.id = :chatId AND m.member.id = :memberId")
    void deleteByChatRoom_IdAndMember_Id(UUID chatId, UUID memberId);

    boolean existsByChatRoom_IdAndMember_Id(UUID chatId, UUID memberId);

    long countByChatRoom_Id(UUID chatId);

    @EntityGraph(attributePaths = { "member" })
    List<ChatRoomMemberEntity> findByChatRoom_Id(UUID chatId);
}
