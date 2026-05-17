package com.optimind.server.module.chat.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.optimind.server.module.auth.UserAuthenticate;
import com.optimind.server.module.chat.dto.MessageDTO;
import com.optimind.server.module.chat.service.ChatService;
import com.optimind.server.module.chat.service.MessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MessageController {
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    // Lấy lịch sử tin nhắn của một phòng chat (Nên có phân trang)
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<MessageDTO.MessageResponse>> getMessageHistory(
            @PathVariable UUID chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(messageService.getMessageHistory(chatId, page, size, getCurrentUserId()));
    }

    // Gửi tin nhắn qua HTTP (Dành cho các file đính kèm hoặc fallback)
    @PostMapping("/chat/{chatId}")
    public ResponseEntity<MessageDTO.MessageResponse> sendMessage(@PathVariable UUID chatId,
            @RequestBody MessageDTO.SendMessageRequest request) {
        MessageDTO.MessageResponse response = messageService.sendMessage(chatId, request, getCurrentUserId());
        broadcastMessage(chatId, response);
        return ResponseEntity.ok(response);
    }

    // Xử lý gửi tin nhắn qua WebSocket
    @MessageMapping("/chat/{chatId}/send")
    public void handleWebSocketMessage(@DestinationVariable UUID chatId,
            MessageDTO.SendMessageRequest request,
            Principal principal) {
        // 1. Kiểm tra principal (được set từ Interceptor)
        if (principal == null) {
            throw new SecurityException("Unauthorized");
        }

        // 2. Lấy UserID từ principal
        UUID userId = getUserIdFromPrincipal(principal);

        // 3. Thực thi service
        MessageDTO.MessageResponse response = messageService.sendMessage(chatId, request, userId);
        broadcastMessage(chatId, response);
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageDTO.MessageResponse> updateMessage(@PathVariable UUID messageId,
            @RequestBody MessageDTO.UpdateMessageRequest request) {
        var result = messageService.updateMessage(messageId, request, getCurrentUserId());
        List<UUID> memberIds = chatService.getMemberIds(result.chatId());
        for (UUID memberId : memberIds) {
            messagingTemplate.convertAndSend("/user/" + memberId + "/update", result.messageResponse());
        }
        return ResponseEntity.ok(result.messageResponse());
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
        UUID chatId = messageService.deleteMessageAndGetChatId(messageId, getCurrentUserId());
        List<UUID> memberIds = chatService.getMemberIds(chatId);
        for (UUID memberId : memberIds) {
            messagingTemplate.convertAndSend("/user/" + memberId + "/delete", messageId);
        }
        return ResponseEntity.noContent().build();
    }

    private void broadcastMessage(UUID chatId, MessageDTO.MessageResponse response) {
        List<UUID> memberIds = chatService.getMemberIds(chatId);
        for (UUID memberId : memberIds) {
            messagingTemplate.convertAndSend("/user/" + memberId + "/notifications", response);
        }
    }

    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserAuthenticate ua)
            return UUID.fromString(ua.getId());
        return UUID.fromString(principal.toString());
    }

    private UUID getUserIdFromPrincipal(Principal principal) {
        // 1. Ưu tiên lấy từ tham số Principal của WebSocket
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            Object userPrincipal = auth.getPrincipal();
            if (userPrincipal instanceof UserAuthenticate ua) {
                return UUID.fromString(ua.getId());
            }
        }

        // 2. Fallback cho các HTTP Request thông thường (nếu dùng chung hàm)
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object contextPrincipal = authentication.getPrincipal();
            if (contextPrincipal instanceof UserAuthenticate ua) {
                return UUID.fromString(ua.getId());
            }
        }

        // 3. Nếu không tìm thấy ở cả 2 nơi, từ chối quyền
        throw new SecurityException("User not authenticated correctly or session expired");
    }
}