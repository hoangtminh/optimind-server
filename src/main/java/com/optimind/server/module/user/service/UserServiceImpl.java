package com.optimind.server.module.user.service;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.optimind.server.module.auth.AuthMapper;
import com.optimind.server.module.user.dto.UserDto;
import com.optimind.server.module.user.dto.LeaderboardUserDto;
import com.optimind.server.module.user.dto.LeaderboardResponse;
import com.optimind.server.module.user.dto.LeaderboardProjection;
import com.optimind.server.module.user.entity.UserEntity;
import com.optimind.server.module.user.repo.UserRepository;
import com.optimind.server.module.task.repo.TaskRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final TaskRepository taskRepository;

    @Override
    public List<UserDto> listUser() {
        return userRepository.findAll().stream()
                .map(authMapper::mapToUserDto)
                .toList();
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UserDto getUserProfile(UUID id) {
        return userRepository.findById(id)
                .map(authMapper::mapToUserDto)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public LeaderboardResponse getLeaderboard(UUID currentUserId) {
        // Fetch top 100 sorted by study time
        List<LeaderboardProjection> projections = 
            userRepository.findLeaderboardTop100(org.springframework.data.domain.PageRequest.of(0, 100));

        List<LeaderboardUserDto> topUsers = new ArrayList<>();
        LeaderboardUserDto currentUserDto = null;
        long currentUserRank = 0;

        for (int i = 0; i < projections.size(); i++) {
            LeaderboardProjection p = projections.get(i);
            int rank = i + 1;
            boolean isMe = p.getId().equals(currentUserId);

            LeaderboardUserDto dto = LeaderboardUserDto.builder()
                .id(p.getId())
                .name(p.getUsername() != null ? p.getUsername() : "User " + rank)
                .avatar(p.getImageUrl())
                .level(p.getLevel() != null ? p.getLevel() : 1)
                .totalStudyTime(p.getStudyTime() != null ? p.getStudyTime() : 0)
                .completedTasks(p.getCompletedTasks() != null ? p.getCompletedTasks() : 0)
                .streak(p.getCurrentStreak() != null ? p.getCurrentStreak() : 0)
                .rank(rank)
                .isCurrentUser(isMe)
                .build();

            topUsers.add(dto);

            if (isMe) {
                currentUserDto = dto;
                currentUserRank = rank;
            }
        }

        // If current user is not in the top 100, fetch their rank and build their details DTO
        if (currentUserDto == null) {
            UserEntity me = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

            currentUserRank = userRepository.calculateRank(me.getStudyTime());
            long completedTasks = taskRepository.countCompletedTasks(currentUserId);

            currentUserDto = LeaderboardUserDto.builder()
                .id(me.getId())
                .name(me.getUsername() != null ? me.getUsername() : "You")
                .avatar(me.getImageUrl())
                .level(me.getLevel() != null ? me.getLevel() : 1)
                .totalStudyTime(me.getStudyTime() != null ? me.getStudyTime() : 0)
                .completedTasks(completedTasks)
                .streak(me.getCurrentStreak() != null ? me.getCurrentStreak() : 0)
                .rank((int) currentUserRank)
                .isCurrentUser(true)
                .build();
        }

        return LeaderboardResponse.builder()
            .topUsers(topUsers)
            .currentUser(currentUserDto)
            .currentUserRank(currentUserRank)
            .build();
    }

    @Override
    public org.springframework.data.domain.Page<UserDto> searchUsers(String query, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return userRepository.searchUsers(query, pageable)
                .map(authMapper::mapToUserDto);
    }

    @Override
    public UserDto suspendUser(UUID id, boolean suspend) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setSuspended(suspend);
        UserEntity updated = userRepository.save(user);
        return authMapper.mapToUserDto(updated);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDto changeUserRole(UUID id, String role) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        try {
            UserEntity.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        
        user.setRole(role.toUpperCase());
        UserEntity updated = userRepository.save(user);
        return authMapper.mapToUserDto(updated);
    }
}
