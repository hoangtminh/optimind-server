package com.optimind.server.module.chat.repo;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optimind.server.module.chat.entity.MessageEntity;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {
    Page<MessageEntity> findByChatRoom_IdOrderByCreatedAtDesc(UUID chatId, Pageable pageable);

    void deleteByChatRoom_Id(UUID chatId);
}
