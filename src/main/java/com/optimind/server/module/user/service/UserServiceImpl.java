package com.optimind.server.module.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.optimind.server.module.auth.AuthMapper;
import com.optimind.server.module.user.dto.UserDto;
import com.optimind.server.module.user.entity.UserEntity;
import com.optimind.server.module.user.repo.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AuthMapper authMapper;

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
}
