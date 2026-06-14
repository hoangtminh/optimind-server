package com.optimind.server.module.user.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardUserDto {
    private UUID id;
    private String name;
    private String avatar;
    private Integer level;
    private Integer totalStudyTime;
    private Long completedTasks;
    private Integer streak;
    private Integer rank;
    private Boolean isCurrentUser;
}
