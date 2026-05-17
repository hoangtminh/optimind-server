package com.optimind.server.module.chat.service;

import java.util.UUID;

import com.optimind.server.module.chat.dto.ChatDTO;

public interface ChatService {
    /**
     * Create a new chat room and add the creator as a member.
     *
     * @param request   create chat request
     * @param creatorId UUID of the user who creates the chat
     * @return details of the newly created chat room
     */
    ChatDTO.ChatResponse createChat(ChatDTO.CreateChatRequest request, UUID creatorId);

    /**
     * List all chat rooms the given user belongs to.
     */
    java.util.List<ChatDTO.ChatResponse> getChatsForUser(UUID userId);

    /**
     * Retrieve chat details including member list. Caller must be a member.
     */
    ChatDTO.ChatDetailResponse getChat(UUID chatId, UUID requesterId);

    /**
     * Update the chat's name or visibility. Requester must be a member.
     */
    ChatDTO.ChatResponse updateChat(UUID chatId, ChatDTO.UpdateChatRequest request, UUID requesterId);

    /**
     * Add a member to a chat by email. Requester must already be a member.
     */
    ChatDTO.ChatMember addMember(UUID chatId, ChatDTO.AddMemberRequest request, UUID requesterId);

    /**
     * Remove a member from a chat. Requester must be a member.
     */
    void removeMember(UUID chatId, UUID memberId, UUID requesterId);

    /**
     * Find a private chat between two users.
     */
    ChatDTO.ChatResponse findPrivateChat(UUID userId, String targetEmail);

    /**
     * Leave a chat. If no members are left, the chat room is deleted.
     */
    void leaveChat(UUID chatId, UUID userId);

    java.util.List<UUID> getMemberIds(UUID chatId);
}
