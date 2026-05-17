package com.optimind.server.module.study.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class StudySessionDTO {

        public record FocusLogData(
                        Instant timestamp,
                        double focusLevel) {
        }

        public record CreateStudySessionRequest(
                        Instant startTime,
                        Instant endTime,
                        long totalTime,
                        Integer focusTime,
                        Integer breakTime,
                        int cycles,
                        double averageFocus,
                        String sessionType,
                        List<FocusLogData> focusData) {
        }

        public record StudySessionResponse(
                        UUID id,
                        Instant startTime,
                        Instant endTime,
                        long totalTime,
                        Integer focusTime,
                        Integer breakTime,
                        int cycles,
                        double averageFocus,
                        String sessionType,
                        Instant createdAt) {
        }

        public record SessionLogResponse(
                        UUID id,
                        Integer focus,
                        Instant timestamp,
                        Instant createdAt) {
        }

        public record SessionTaskResponse(
                        Long id,
                        String title,
                        boolean isCompleted,
                        Instant createdAt) {
        }

        public record SessionDetailsResponse(
                        StudySessionResponse session,
                        List<SessionLogResponse> logs,
                        List<SessionTaskResponse> tasks) {
        }
}