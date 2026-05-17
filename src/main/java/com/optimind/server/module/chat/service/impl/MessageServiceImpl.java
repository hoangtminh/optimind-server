package com.optimind.server.module.chat.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimind.server.module.chat.dto.MessageDTO;
import com.optimind.server.module.chat.entity.ChatRoomEnity;
import com.optimind.server.module.chat.entity.MessageEntity;
import com.optimind.server.module.chat.repo.ChatRoomMemberRepository;
import com.optimind.server.module.chat.repo.ChatRoomRepository;
import com.optimind.server.module.chat.repo.MessageRepository;
import com.optimind.server.module.chat.service.MessageService;
import com.optimind.server.module.user.dto.UserDto;
import com.optimind.server.module.user.entity.UserEntity;
import com.optimind.server.module.user.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO.MessageResponse> getMessageHistory(UUID chatId, int page, int size, UUID requesterId) {
        verifyMembership(chatId, requesterId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return messageRepository.findByChatRoom_IdOrderByCreatedAtDesc(chatId, pageable)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Override
    @Transactional
    public MessageDTO.MessageResponse sendMessage(UUID chatId, MessageDTO.SendMessageRequest request, UUID authorId) {
        verifyMembership(chatId, authorId);
        ChatRoomEnity chat = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
        UserEntity author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        MessageEntity message = MessageEntity.builder().text(request.text()).author(author).chatRoom(chat).build();
        chat.setLastActive(java.time.Instant.now());
        chat.setLastMessage(request.text());
        chatRoomRepository.save(chat);
        message = messageRepository.save(message);
        return toMessageResponse(message);
    }

    @Override
    @Transactional
    public UUID deleteMessageAndGetChatId(UUID messageId, UUID requesterId) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        if (!message.getAuthor().getId().equals(requesterId))
            throw new SecurityException("Not authorized to delete this message");
        UUID chatId = message.getChatRoom().getId();
        messageRepository.delete(message);
        return chatId;
    }

    @Override
    @Transactional
    public UpdateResult updateMessage(UUID messageId, MessageDTO.UpdateMessageRequest request, UUID requesterId) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        if (!message.getAuthor().getId().equals(requesterId)) {
            throw new SecurityException("Not authorized to update this message");
        }
        message.setText(request.text());
        message = messageRepository.save(message);
        return new UpdateResult(toMessageResponse(message), message.getChatRoom().getId());
    }

    private void verifyMembership(UUID chatId, UUID userId) {
        if (!chatRoomMemberRepository.existsByChatRoom_IdAndMember_Id(chatId, userId))
            throw new SecurityException("User is not a member of this chat");
    }

    private MessageDTO.MessageResponse toMessageResponse(MessageEntity m) {
        UserEntity author = m.getAuthor();
        UserDto authorDto = UserDto.builder()
                .id(author.getId())
                .email(author.getEmail())
                .username(author.getUsername())
                .imageUrl(author.getImageUrl())
                .build();
        return new MessageDTO.MessageResponse(m.getId(), m.getText(), authorDto, m.getCreatedAt(),
                m.getChatRoom().getId());
    }
}