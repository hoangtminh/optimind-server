package com.optimind.server.module.user.service;

import java.util.List;
import java.util.UUID;

import com.optimind.server.module.user.dto.UserDto;
import com.optimind.server.module.user.entity.UserEntity;

public interface UserService {
    List<UserDto> listUser();

    UserEntity getUserByEmail(String email);

    UserDto getUserProfile(UUID id);

    com.optimind.server.module.user.dto.LeaderboardResponse getLeaderboard(UUID currentUserId);
}
