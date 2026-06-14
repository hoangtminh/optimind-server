package com.optimind.server.module.user.dto;

import java.util.UUID;

public interface LeaderboardProjection {
    UUID getId();
    String getUsername();
    String getImageUrl();
    Integer getLevel();
    Integer getStudyTime();
    Integer getCurrentStreak();
    Long getCompletedTasks();
}
