package com.optimind.server.module.setting.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.optimind.server.module.auth.UserAuthenticate;
import com.optimind.server.module.setting.dto.UserSettingDTO;
import com.optimind.server.module.setting.service.UserSettingService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserSettingController {

    private final UserSettingService service;

    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserAuthenticate ua) {
            return UUID.fromString(ua.getId());
        }
        return UUID.fromString(principal.toString());
    }

    @GetMapping
    public ResponseEntity<UserSettingDTO.Response> getSettings() {
        UserSettingDTO.Response response = service.getSettings(getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<UserSettingDTO.Response> updateSettings(@RequestBody UserSettingDTO.UpdateRequest request) {
        UserSettingDTO.Response response = service.updateSettings(getCurrentUserId(), request);
        return ResponseEntity.ok(response);
    }
}
