package com.optimind.server.module.user.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String email;

    private String username;

    private String password;
    private String role;

    private String imageUrl;

    @Builder.Default
    private Integer coins = 0;

    @Builder.Default
    private Integer currentStreak = 0;

    @Builder.Default
    private Integer exp = 0;

    private Instant lastActiveDate;

    @Builder.Default
    private Integer level = 1;

    @Builder.Default
    private Integer longestStreak = 0;

    @Builder.Default
    private Integer studyTime = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    public enum Role {
        USER, ADMIN
    }
}
