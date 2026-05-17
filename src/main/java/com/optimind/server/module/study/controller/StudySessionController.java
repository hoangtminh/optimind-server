package com.optimind.server.module.study.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimind.server.module.auth.UserAuthenticate;
import com.optimind.server.module.study.dto.StudySessionDTO;
import com.optimind.server.module.study.service.StudySessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/study-sessions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class StudySessionController {

    private final StudySessionService studySessionService;

    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserAuthenticate ua) {
            return UUID.fromString(ua.getId());
        }
        return UUID.fromString(principal.toString());
    }

    @PostMapping
    public ResponseEntity<StudySessionDTO.StudySessionResponse> createSession(
            @RequestBody StudySessionDTO.CreateStudySessionRequest request) {
        StudySessionDTO.StudySessionResponse response = studySessionService.createSession(request, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<StudySessionDTO.StudySessionResponse>> getSessions() {
        List<StudySessionDTO.StudySessionResponse> response = studySessionService
                .getSessionsForUser(getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<StudySessionDTO.SessionDetailsResponse> getSessionDetails(@PathVariable UUID sessionId) {
        StudySessionDTO.SessionDetailsResponse response = studySessionService.getSessionDetails(sessionId,
                getCurrentUserId());
        return ResponseEntity.ok(response);
    }
}