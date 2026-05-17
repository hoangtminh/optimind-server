package com.optimind.server.module.task.service.impl;

import com.optimind.server.module.task.entity.ProjectEntity;
import com.optimind.server.module.task.repo.ProjectRepository;
import com.optimind.server.module.task.dto.TaskDTO;
import com.optimind.server.module.task.entity.TaskEntity;
import com.optimind.server.module.task.repo.TaskRepository;
import com.optimind.server.module.task.service.TaskService;
import com.optimind.server.module.user.entity.UserEntity;
import com.optimind.server.module.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

        private final TaskRepository taskRepository;
        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;

        @Override
        @Transactional
        public TaskDTO.TaskResponse createTask(TaskDTO.CreateTaskRequest request, UUID userId) {
                UserEntity user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                ProjectEntity project = projectRepository.findById(request.projectId())
                                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

                if (!project.getUser().getId().equals(userId)) {
                        throw new SecurityException("User does not have access to this project");
                }

                System.out.println(request);

                TaskEntity task = TaskEntity.builder()
                                .user(user)
                                .project(project)
                                .title(request.title())
                                .note(request.note())
                                .status(request.status())
                                .dueDate(request.dueDate())
                                .repeated(request.repeated())
                                .tag(request.tag())
                                .priority(request.priority())
                                .isCompleted(request.status() != null && request.status().equalsIgnoreCase("complete"))
                                .build();

                task = taskRepository.save(task);
                int currentCount = project.getTaskCount() == null ? 0 : project.getTaskCount();
                project.setTaskCount(currentCount + 1);
                projectRepository.save(project);
                return toResponse(task);
        }

        @Override
        public List<TaskDTO.TaskResponse> getAllTasksForUser(UUID userId) {
                // Assuming a method like findByUser_IdOrderByCreatedAtDesc exists
                return taskRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public List<TaskDTO.TaskResponse> getTasksByProject(UUID projectId, UUID userId) {
                projectRepository.findById(projectId)
                                .filter(p -> p.getUser().getId().equals(userId))
                                .orElseThrow(() -> new SecurityException("User does not have access to this project"));

                // Assuming a method like findByProjectIdAndUser_IdOrderByCreatedAtDesc exists
                return taskRepository.findByProjectIdAndUser_IdOrderByCreatedAtDesc(projectId, userId).stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public TaskDTO.TaskResponse updateTask(UUID taskId, TaskDTO.UpdateTaskRequest request, UUID userId) {
                TaskEntity task = taskRepository.findByIdAndUserId(taskId, userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Task not found or user not authorized"));

                if (request.title() != null)
                        task.setTitle(request.title());

                // Optional fields (can be set to null)
                task.setNote(request.note());
                task.setDueDate(request.dueDate());
                task.setTag(request.tag());

                if (request.status() != null) {
                        task.setStatus(request.status());
                        task.setCompleted(request.status().equalsIgnoreCase("complete"));
                }
                if (request.repeated() != null)
                        task.setRepeated(request.repeated());
                if (request.priority() != null)
                        task.setPriority(request.priority());

                task = taskRepository.save(task);
                return toResponse(task);
        }

        @Override
        public TaskDTO.TaskResponse getTaskById(UUID taskId, UUID userId) {
                TaskEntity task = taskRepository.findByIdAndUserId(taskId, userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Task not found or user not authorized"));

                return toResponse(task);
        }

        @Override
        @Transactional
        public TaskDTO.TaskResponse updateTaskStatus(UUID taskId, String newStatus, UUID userId) {
                TaskEntity task = taskRepository.findByIdAndUserId(taskId, userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Task not found or user not authorized"));

                if (newStatus != null) {
                        task.setStatus(newStatus);
                        task.setCompleted(newStatus.equalsIgnoreCase("complete"));
                }

                task = taskRepository.save(task);
                return toResponse(task);
        }

        @Override
        @Transactional
        public void deleteTask(UUID taskId, UUID userId) {
                TaskEntity task = taskRepository.findByIdAndUserId(taskId, userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Task not found or user not authorized"));

                ProjectEntity project = task.getProject();
                taskRepository.delete(task);

                if (project != null) {
                        int currentCount = project.getTaskCount() == null ? 0 : project.getTaskCount();
                        project.setTaskCount(Math.max(0, currentCount - 1));
                        projectRepository.save(project);
                }
        }

        private TaskDTO.TaskResponse toResponse(TaskEntity entity) {
                return new TaskDTO.TaskResponse(
                                entity.getId(),
                                entity.getTitle(),
                                entity.getNote(),
                                entity.getDueDate(),
                                entity.isCompleted(),
                                entity.getTag(),
                                entity.getRepeated(),
                                entity.getStatus(),
                                entity.getPriority(),
                                entity.getProject().getId(),
                                entity.getCreatedAt(),
                                entity.getUpdatedAt());
        }
}