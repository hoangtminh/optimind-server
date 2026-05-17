package com.optimind.server.module.task.service;

import com.optimind.server.module.task.dto.TaskDTO;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskDTO.TaskResponse createTask(TaskDTO.CreateTaskRequest request, UUID userId);

    List<TaskDTO.TaskResponse> getAllTasksForUser(UUID userId);

    List<TaskDTO.TaskResponse> getTasksByProject(UUID projectId, UUID userId);

    TaskDTO.TaskResponse updateTask(UUID taskId, TaskDTO.UpdateTaskRequest request, UUID userId);

    TaskDTO.TaskResponse getTaskById(UUID taskId, UUID userId);

    TaskDTO.TaskResponse updateTaskStatus(UUID taskId, String newStatus, UUID userId);

    void deleteTask(UUID taskId, UUID userId);
}