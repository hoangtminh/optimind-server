package com.optimind.server.module.friend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import com.optimind.server.module.user.entity.UserEntity;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "friendships")
public class FriendshipEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id_1")
    private UserEntity user1;

    @ManyToOne
    @JoinColumn(name = "user_id_2")
    private UserEntity user2;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
