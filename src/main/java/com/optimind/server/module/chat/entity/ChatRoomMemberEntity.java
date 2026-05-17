package com.optimind.server.module.chat.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.optimind.server.module.user.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_room_member")
@IdClass(ChatRoomMemberId.class)
public class ChatRoomMemberEntity {
    @Id
    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoomEnity chatRoom;

    @Id
    @ManyToOne
    @JoinColumn(name = "member_id")
    private UserEntity member;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
