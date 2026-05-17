package com.optimind.server.module.task.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TaskDTO {

        public record CreateTaskRequest(
                        UUID projectId,
                        String title,
                        String note, // Optional
                        String status,
                        Instant dueDate, // Optional
                        String repeated,
                        List<String> tag, // Optional
                        String priority) {
        }

        public record UpdateTaskRequest(
                        String title,
                        String note, // Optional
                        Instant dueDate, // Optional
                        String status,
                        String repeated,
                        List<String> tag, // Optional
                        String priority) {
        }

        public record UpdateTaskStatusRequest(
                        String status) {
        }

        public record TaskResponse(
                        UUID id,
                        String title,
                        String note,
                        Instant dueDate,
                        boolean isCompleted,
                        List<String> tag,
                        String repeated,
                        String status,
                        String priority,
                        UUID projectId,
                        Instant createdAt,
                        Instant updatedAt) {
        }
}