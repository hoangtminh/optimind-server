package com.optimind.server.module.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.optimind.server.module.user.dto.UserDto;
import com.optimind.server.module.user.entity.UserEntity;
import com.optimind.server.module.user.service.UserService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    public List<UserDto> getUsers() {
        List<UserDto> listUserDto = userService.listUser();
        return listUserDto;
    }

    @GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<UserDto>> searchUsers(
            @org.springframework.web.bind.annotation.RequestParam(value = "query", required = false) String query,
            @org.springframework.web.bind.annotation.RequestParam(value = "page", defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.searchUsers(query, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<com.optimind.server.module.user.dto.LeaderboardResponse> getLeaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard(getCurrentUserId()));
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> suspendUser(
            @PathVariable UUID id,
            @RequestParam("suspended") boolean suspended) {
        return ResponseEntity.ok(userService.suspendUser(id, suspended));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> changeUserRole(
            @PathVariable UUID id,
            @RequestParam("role") String role) {
        if (id.equals(getCurrentUserId())) {
            throw new IllegalArgumentException("You cannot change your own role.");
        }
        return ResponseEntity.ok(userService.changeUserRole(id, role));
    }

    private UUID getCurrentUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof com.optimind.server.module.auth.UserAuthenticate ua) {
            return UUID.fromString(ua.getId());
        }
        return UUID.fromString(principal.toString());
    }
}
