package com.optimind.server.security;

import java.time.Instant;

import com.optimind.server.module.user.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class TokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    private boolean revoked;
    private Instant expiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}