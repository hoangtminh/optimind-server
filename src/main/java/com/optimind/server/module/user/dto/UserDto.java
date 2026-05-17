package com.optimind.server.module.user.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private String username;
    private String role;
    private String imageUrl;
    private Integer coins;
    private Integer currentStreak;
    private Integer exp;
    private Instant lastActiveDate;
    private Integer level;
    private Integer longestStreak;
    private Integer studyTime;
    private Instant createdAt;
}
