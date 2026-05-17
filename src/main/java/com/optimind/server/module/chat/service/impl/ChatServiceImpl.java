package com.optimind.server.module.chat.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimind.server.module.chat.dto.ChatDTO;
import com.optimind.server.module.chat.entity.ChatRoomEnity;
import com.optimind.server.module.chat.entity.ChatRoomMemberEntity;
import com.optimind.server.module.chat.repo.ChatRoomMemberRepository;
import com.optimind.server.module.chat.repo.ChatRoomRepository;
import com.optimind.server.module.chat.repo.MessageRepository;
import com.optimind.server.module.chat.service.ChatService;
import com.optimind.server.module.user.entity.UserEntity;
import com.optimind.server.module.user.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

        private final ChatRoomRepository chatRoomRepository;
        private final ChatRoomMemberRepository chatRoomMemberRepository;
        private final UserRepository userRepository;
        private final MessageRepository messageRepository;

        @Override
        @Transactional
        public ChatDTO.ChatResponse createChat(ChatDTO.CreateChatRequest request, UUID creatorId) {
                // Logic to find private chat or create a new one
                if (!request.isPublic() && request.members() != null && request.members().size() == 1) {
                        UserEntity creator = userRepository.findById(creatorId)
                                        .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
                        String otherMemberEmail = request.members().get(0);
                        UserEntity otherMember = userRepository.findByEmail(otherMemberEmail)
                                        .orElseThrow(() -> new IllegalArgumentException("Other member not found"));

                        Optional<ChatRoomEnity> existingChat = chatRoomRepository.findPrivateChatBetweenUsers(
                                        creator.getId(),
                                        otherMember.getId());

                        if (existingChat.isPresent()) {
                                return toChatResponse(existingChat.get());
                        }
                }

                ChatRoomEnity chat = ChatRoomEnity.builder()
                                .name(request.name())
                                .isPublic(request.isPublic())
                                .build();
                ChatRoomEnity createdChat = chatRoomRepository.save(chat);

                addMemberToRoom(createdChat, creatorId);

                System.out.println("Members: " + request);
                if (request.members() != null) {
                        for (String email : request.members()) {
                                System.out.println(email);
                                userRepository.findByEmail(email)
                                                .ifPresent(user -> addMemberToRoom(createdChat, user.getId()));
                        }
                }

                return toChatResponse(createdChat);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ChatDTO.ChatResponse> getChatsForUser(UUID userId) {
                // Sử dụng Join Fetch trong Repo để tránh N+1
                return chatRoomMemberRepository.findByMember_IdOrderByChatRoom_LastActiveDesc(userId)
                                .stream()
                                .map(m -> toChatResponse(m.getChatRoom()))
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public ChatDTO.ChatDetailResponse getChat(UUID chatId, UUID requesterId) {
                verifyMembership(chatId, requesterId);
                ChatRoomEnity chat = chatRoomRepository.findById(chatId)
                                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

                List<ChatDTO.ChatMember> members = chatRoomMemberRepository.findByChatRoom_Id(chatId)
                                .stream()
                                .map(memberEntity -> {
                                        UserEntity user = memberEntity.getMember();
                                        return new ChatDTO.ChatMember(user.getId(), user.getUsername(), user.getEmail(),
                                                        user.getImageUrl());
                                })
                                .collect(Collectors.toList());

                return new ChatDTO.ChatDetailResponse(chat.getId(), chat.getName(), chat.isPublic(),
                                chat.getLastActive(),
                                chat.getCreatedAt(), members, chat.getLastMessage());
        }

        @Override
        @Transactional
        public ChatDTO.ChatResponse updateChat(UUID chatId, ChatDTO.UpdateChatRequest request, UUID requesterId) {
                verifyMembership(chatId, requesterId);
                ChatRoomEnity chat = chatRoomRepository.findById(chatId)
                                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

                if (request.name() != null) {
                        chat.setName(request.name());
                }
                if (request.isPublic() != null) {
                        chat.setPublic(request.isPublic());
                }

                chat = chatRoomRepository.save(chat);
                return toChatResponse(chat);
        }

        @Override
        @Transactional
        public ChatDTO.ChatMember addMember(UUID chatId, ChatDTO.AddMemberRequest request, UUID requesterId) {
                verifyMembership(chatId, requesterId);
                ChatRoomEnity chat = chatRoomRepository.findById(chatId)
                                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
                UserEntity userToAdd = userRepository.findByEmail(request.email())
                                .orElseThrow(() -> new IllegalArgumentException("User with given email not found"));

                addMemberToRoom(chat, userToAdd.getId());

                return new ChatDTO.ChatMember(userToAdd.getId(), userToAdd.getUsername(), userToAdd.getEmail(),
                                userToAdd.getImageUrl());
        }

        @Override
        @Transactional
        public void removeMember(UUID chatId, UUID memberId, UUID requesterId) {
                verifyMembership(chatId, requesterId);
                // Tối ưu: Xóa trực tiếp bằng query
                chatRoomMemberRepository.deleteByChatRoom_IdAndMember_Id(chatId, memberId);
        }

        @Override
        @Transactional(readOnly = true)
        public ChatDTO.ChatResponse findPrivateChat(UUID userId, String targetEmail) {
                UserEntity targetUser = userRepository.findByEmail(targetEmail)
                                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

                return chatRoomRepository.findPrivateChatBetweenUsers(userId, targetUser.getId())
                                .map(this::toChatResponse)
                                .orElse(null); // Or throw exception if chat must exist
        }

        @Override
        @Transactional
        public void leaveChat(UUID chatId, UUID userId) {
                verifyMembership(chatId, userId);
                chatRoomMemberRepository.deleteByChatRoom_IdAndMember_Id(chatId, userId);

                // If no members left, delete messages and chat room
                if (chatRoomMemberRepository.countByChatRoom_Id(chatId) == 0) {
                        messageRepository.deleteByChatRoom_Id(chatId);
                        chatRoomRepository.deleteById(chatId);
                }
        }

        @Override
        @Transactional(readOnly = true)
        public List<UUID> getMemberIds(UUID chatId) {
                return chatRoomMemberRepository.findByChatRoom_Id(chatId).stream()
                                .map(m -> m.getMember().getId())
                                .toList();
        }

        private void verifyMembership(UUID chatId, UUID userId) {
                if (!chatRoomMemberRepository.existsByChatRoom_IdAndMember_Id(chatId, userId)) {
                        throw new SecurityException("User is not a member of this chat");
                }
        }

        // Helper: Thêm thành viên vào thực thể
        private void addMemberToRoom(ChatRoomEnity chat, UUID userId) {
                UserEntity user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                if (!chatRoomMemberRepository.existsByChatRoom_IdAndMember_Id(chat.getId(), userId)) {
                        ChatRoomMemberEntity membership = ChatRoomMemberEntity.builder()
                                        .chatRoom(chat)
                                        .member(user)
                                        .build();
                        chatRoomMemberRepository.save(membership);
                }
        }

        // Mapping logic (Nên tách ra để code sạch hơn)
        private ChatDTO.ChatResponse toChatResponse(ChatRoomEnity c) {
                return new ChatDTO.ChatResponse(c.getId(), c.getName(), c.isPublic(), c.getLastActive(),
                                c.getCreatedAt(), c.getLastMessage());
        }
}