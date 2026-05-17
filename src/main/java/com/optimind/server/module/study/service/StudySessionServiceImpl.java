package com.optimind.server.module.study.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimind.server.module.study.dto.StudySessionDTO;
import com.optimind.server.module.study.entity.SessionLogEntity;
import com.optimind.server.module.study.entity.SessionTaskEntity;
import com.optimind.server.module.study.entity.StudySessionEntity;
import com.optimind.server.module.study.repo.SessionLogRepository;
import com.optimind.server.module.study.repo.SessionTaskRepository;
import com.optimind.server.module.study.repo.StudySessionRepository;
import com.optimind.server.module.user.entity.UserEntity;
import com.optimind.server.module.user.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudySessionServiceImpl implements StudySessionService {

        private final StudySessionRepository studySessionRepository; // Assuming this repository exists
        private final SessionLogRepository sessionLogRepository; // Assuming this repository exists
        private final SessionTaskRepository sessionTaskRepository; // Assuming this repository exists
        private final UserRepository userRepository;

        @Override
        @Transactional
        public StudySessionDTO.StudySessionResponse createSession(StudySessionDTO.CreateStudySessionRequest request,
                        UUID userId) {
                UserEntity user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                StudySessionEntity session = StudySessionEntity.builder()
                                .user(user)
                                .startTime(request.startTime())
                                .endTime(request.endTime())
                                .totalTime(request.totalTime())
                                .focusTime(request.focusTime())
                                .breakTime(request.breakTime())
                                .cycles(request.cycles())
                                .averageFocus(request.focusData() != null && !request.focusData().isEmpty()
                                                ? request.averageFocus() / request.focusData().size()
                                                : 0)
                                .sessionType(request.sessionType())
                                .build();

                session = studySessionRepository.save(session);

                if (request.focusData() != null && !request.focusData().isEmpty()) {
                        StudySessionEntity finalSession = session;
                        List<SessionLogEntity> logs = request.focusData().stream()
                                        .map(log -> SessionLogEntity.builder()
                                                        .session(finalSession)
                                                        .user(user)
                                                        .timestamp(log.timestamp())
                                                        .focus((int) log.focusLevel())
                                                        .build())
                                        .collect(Collectors.toList());
                        sessionLogRepository.saveAll(logs);
                }

                return toResponse(session);
        }

        @Override
        public List<StudySessionDTO.StudySessionResponse> getSessionsForUser(UUID userId) {
                return studySessionRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }

        private StudySessionDTO.StudySessionResponse toResponse(StudySessionEntity entity) {
                return new StudySessionDTO.StudySessionResponse(
                                entity.getId(),
                                entity.getStartTime(),
                                entity.getEndTime(),
                                entity.getTotalTime(),
                                entity.getFocusTime(),
                                entity.getBreakTime(),
                                entity.getCycles(),
                                entity.getAverageFocus(),
                                entity.getSessionType(),
                                entity.getCreatedAt());
        }

        @Override
        public StudySessionDTO.SessionDetailsResponse getSessionDetails(UUID sessionId, UUID userId) {
                StudySessionEntity session = studySessionRepository.findById(sessionId)
                                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

                if (!session.getUser().getId().equals(userId)) {
                        throw new IllegalArgumentException("Access denied");
                }

                List<StudySessionDTO.SessionLogResponse> logs = sessionLogRepository.findBySession_Id(sessionId)
                                .stream()
                                .map(this::toLogResponse)
                                .collect(Collectors.toList());

                List<StudySessionDTO.SessionTaskResponse> tasks = sessionTaskRepository.findBySession_Id(sessionId)
                                .stream()
                                .map(this::toTaskResponse)
                                .collect(Collectors.toList());

                return new StudySessionDTO.SessionDetailsResponse(toResponse(session), logs, tasks);
        }

        private StudySessionDTO.SessionLogResponse toLogResponse(SessionLogEntity entity) {
                return new StudySessionDTO.SessionLogResponse(
                                entity.getId(),
                                entity.getFocus(),
                                entity.getTimestamp(),
                                entity.getCreatedAt());
        }

        private StudySessionDTO.SessionTaskResponse toTaskResponse(SessionTaskEntity entity) {
                return new StudySessionDTO.SessionTaskResponse(
                                entity.getId(),
                                entity.getTitle(),
                                entity.isCompleted(),
                                entity.getCreatedAt());
        }
}