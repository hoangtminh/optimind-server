package com.optimind.server.module.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardResponse {
    private List<LeaderboardUserDto> topUsers;
    private LeaderboardUserDto currentUser;
    private long currentUserRank;
}
