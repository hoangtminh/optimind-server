package com.optimind.server.module.chat.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Data transfer objects for chat module operations.
 */
public class ChatDTO {
        public record CreateChatRequest(String name, List<String> members, boolean isPublic) {
        }

        public record UpdateChatRequest(String name, Boolean isPublic) {
        }

        public record AddMemberRequest(String email) {
        }

        public record ChatMember(UUID id, String username, String email, String image_url) {
        }

        public record ChatResponse(UUID id, String name, boolean isPublic, Instant lastActive, Instant createdAt,
                        String lastMessage) {
        }

        public record ChatDetailResponse(UUID id, String name, boolean isPublic, Instant lastActive, Instant createdAt,
                        java.util.List<ChatMember> members, String lastMessage) {
        }
}
