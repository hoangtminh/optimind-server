package com.optimind.server.module.chat.service;

import java.util.List;
import java.util.UUID;

import com.optimind.server.module.chat.dto.MessageDTO;

public interface MessageService {
    List<MessageDTO.MessageResponse> getMessageHistory(UUID chatId, int page, int size, UUID requesterId);

    MessageDTO.MessageResponse sendMessage(UUID chatId, MessageDTO.SendMessageRequest request, UUID authorId);

    UUID deleteMessageAndGetChatId(UUID messageId, UUID requesterId);

    record UpdateResult(MessageDTO.MessageResponse messageResponse, UUID chatId) {
    }

    UpdateResult updateMessage(UUID messageId, MessageDTO.UpdateMessageRequest request, UUID requesterId);
}