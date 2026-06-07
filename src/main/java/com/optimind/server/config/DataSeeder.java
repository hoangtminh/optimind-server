package com.optimind.server.config;

import com.optimind.server.module.chat.entity.ChatRoomEnity;
import com.optimind.server.module.chat.entity.ChatRoomMemberEntity;
import com.optimind.server.module.chat.entity.MessageEntity;
import com.optimind.server.module.chat.repo.ChatRoomMemberRepository;
import com.optimind.server.module.chat.repo.ChatRoomRepository;
import com.optimind.server.module.chat.repo.MessageRepository;
import com.optimind.server.module.friend.entity.FriendshipEntity;
import com.optimind.server.module.friend.repo.FriendshipRepository;
import com.optimind.server.module.task.entity.ProjectEntity;
import com.optimind.server.module.task.entity.TaskEntity;
import com.optimind.server.module.task.repo.ProjectRepository;
import com.optimind.server.module.task.repo.TaskRepository;
import com.optimind.server.module.user.entity.UserEntity;
import com.optimind.server.module.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final FriendshipRepository friendshipRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting database seeding...");

        // 1. Seed Users
        UserEntity user1 = seedUser("hoangtuanminh25@gmail.com", "Hoang Tuan Minh", "123");
        UserEntity user2 = seedUser("friendcraftdemo@gmail.com", "Friend Craft Demo", "123");

        // 2. Seed Friendship (make them friends)
        seedFriendship(user1, user2);

        // 3. Seed Projects & Tasks for User 1
        ProjectEntity proj1 = seedProject(user1, "Optimind App Development", "Main development project for Optimind");
        seedTask(user1, proj1, "Design Kanban Board Dnd", "Fix the bug when dragging a task across columns.", "TODO", "HIGH");
        seedTask(user1, proj1, "Implement Search Friend By Email", "Connect frontend UI to backend API endpoint.", "IN_PROGRESS", "MEDIUM");
        seedTask(user1, proj1, "Clean Code Audit", "Refactor controllers and components using best practices.", "DONE", "LOW");

        // Seed Projects & Tasks for User 2
        ProjectEntity proj2 = seedProject(user2, "FriendCraft Design System", "Creating design guidelines for FriendCraft");
        seedTask(user2, proj2, "Draft UI Layout Mockups", "Create premium Figma layouts.", "TODO", "HIGH");
        seedTask(user2, proj2, "Define Typography System", "Select fonts, weights, sizes.", "DONE", "MEDIUM");

        // 4. Seed Chat Room
        ChatRoomEnity chatRoom = seedChatRoom(user1, user2, "Hoang & FriendCraft Chat");

        // 5. Seed Messages
        seedMessage(user1, chatRoom, "Hello! How is the project going?");
        seedMessage(user2, chatRoom, "Hi Hoang! I am working on the typography styles now. How about you?");
        seedMessage(user1, chatRoom, "Great! I just finished the Kanban board drag and drop bug fix.");
        seedMessage(user2, chatRoom, "Awesome work! Let's integrate the friends search next.");

        log.info("Database seeding completed successfully!");
    }

    private UserEntity seedUser(String email, String username, String rawPassword) {
        Optional<UserEntity> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }

        UserEntity user = UserEntity.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(UserEntity.Role.USER.toString())
                .coins(100)
                .currentStreak(5)
                .exp(500)
                .level(3)
                .lastActiveDate(Instant.now())
                .build();

        return userRepository.save(user);
    }

    private void seedFriendship(UserEntity user1, UserEntity user2) {
        Optional<FriendshipEntity> existing = friendshipRepository.findFriendshipBetween(user1.getId(), user2.getId());
        if (existing.isPresent()) {
            return;
        }

        FriendshipEntity friendship = FriendshipEntity.builder()
                .user1(user1)
                .user2(user2)
                .build();
        friendshipRepository.save(friendship);
        log.info("Seeded friendship between {} and {}", user1.getEmail(), user2.getEmail());
    }

    private ProjectEntity seedProject(UserEntity user, String name, String desc) {
        List<ProjectEntity> projects = projectRepository.findByUser_Id(user.getId());
        for (ProjectEntity p : projects) {
            if (p.getName().equals(name)) {
                return p;
            }
        }

        ProjectEntity proj = ProjectEntity.builder()
                .name(name)
                .description(desc)
                .user(user)
                .taskCount(0)
                .build();
        return projectRepository.save(proj);
    }

    private void seedTask(UserEntity user, ProjectEntity project, String title, String note, String status, String priority) {
        List<TaskEntity> tasks = taskRepository.findByProjectIdAndUser_IdOrderByCreatedAtDesc(project.getId(), user.getId());
        for (TaskEntity t : tasks) {
            if (t.getTitle().equals(title)) {
                return;
            }
        }

        TaskEntity task = TaskEntity.builder()
                .title(title)
                .note(note)
                .dueDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .isCompleted("DONE".equals(status))
                .repeated("NONE")
                .status(status)
                .priority(priority)
                .project(project)
                .user(user)
                .tag(List.of("Seed"))
                .build();

        taskRepository.save(task);
        
        project.setTaskCount(project.getTaskCount() + 1);
        projectRepository.save(project);
    }

    private ChatRoomEnity seedChatRoom(UserEntity user1, UserEntity user2, String name) {
        Optional<ChatRoomEnity> existing = chatRoomRepository.findPrivateChatBetweenUsers(user1.getId(), user2.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        ChatRoomEnity room = ChatRoomEnity.builder()
                .name(name)
                .isPublic(false)
                .lastActive(Instant.now())
                .lastMessage("Welcome to the chat!")
                .build();
        room = chatRoomRepository.save(room);

        ChatRoomMemberEntity m1 = ChatRoomMemberEntity.builder()
                .chatRoom(room)
                .member(user1)
                .build();
        ChatRoomMemberEntity m2 = ChatRoomMemberEntity.builder()
                .chatRoom(room)
                .member(user2)
                .build();

        chatRoomMemberRepository.save(m1);
        chatRoomMemberRepository.save(m2);

        log.info("Seeded chat room: {}", room.getName());
        return room;
    }

    private void seedMessage(UserEntity author, ChatRoomEnity chatRoom, String text) {
        MessageEntity msg = MessageEntity.builder()
                .author(author)
                .chatRoom(chatRoom)
                .text(text)
                .build();
        messageRepository.save(msg);
        chatRoom.setLastMessage(text);
        chatRoom.setLastActive(Instant.now());
        chatRoomRepository.save(chatRoom);
    }
}
