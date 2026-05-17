package com.optimind.server.module.task.controller;

import com.optimind.server.module.auth.UserAuthenticate;
import com.optimind.server.module.task.dto.TaskDTO;
import com.optimind.server.module.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TaskController {

    private final TaskService taskService;

    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserAuthenticate ua) {
            return UUID.fromString(ua.getId());
        }
        return UUID.fromString(principal.toString());
    }

    @PostMapping
    public ResponseEntity<TaskDTO.TaskResponse> createTask(@RequestBody TaskDTO.CreateTaskRequest request) {
        TaskDTO.TaskResponse response = taskService.createTask(request, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO.TaskResponse>> getAllTasks() {
        System.out.println(taskService.getAllTasksForUser(getCurrentUserId()));
        return ResponseEntity.ok(taskService.getAllTasksForUser(getCurrentUserId()));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDTO.TaskResponse>> getTasksByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId, getCurrentUserId()));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO.TaskResponse> getTaskById(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId, getCurrentUserId()));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO.TaskResponse> updateTask(@PathVariable UUID taskId,
            @RequestBody TaskDTO.UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request, getCurrentUserId()));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskDTO.TaskResponse> updateTaskStatus(@PathVariable UUID taskId,
            @RequestBody TaskDTO.UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, request.status(), getCurrentUserId()));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId) {
        taskService.deleteTask(taskId, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}