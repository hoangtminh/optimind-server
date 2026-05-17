package com.optimind.server.module.study.service;

import java.util.List;
import java.util.UUID;

import com.optimind.server.module.study.dto.StudySessionDTO;

public interface StudySessionService {
    StudySessionDTO.StudySessionResponse createSession(StudySessionDTO.CreateStudySessionRequest request, UUID userId);

    List<StudySessionDTO.StudySessionResponse> getSessionsForUser(UUID userId);

    StudySessionDTO.SessionDetailsResponse getSessionDetails(UUID sessionId, UUID userId);
}