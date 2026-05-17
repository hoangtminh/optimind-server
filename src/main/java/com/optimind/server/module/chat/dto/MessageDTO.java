package com.optimind.server.module.chat.dto;

import java.time.Instant;
import java.util.UUID;

import com.optimind.server.module.user.dto.UserDto;

public class MessageDTO {
    public record SendMessageRequest(String text) {
    }

    public record UpdateMessageRequest(UUID id, String text, UUID chatId) {
    }

    public record MessageResponse(UUID id, String text, UserDto author, Instant createdAt, UUID chatId) {
    }
}