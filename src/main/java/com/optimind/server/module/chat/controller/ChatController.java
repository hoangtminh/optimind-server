package com.optimind.server.module.chat.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.optimind.server.module.chat.dto.ChatDTO;
import com.optimind.server.module.chat.service.ChatService;
import com.optimind.server.module.auth.UserAuthenticate;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChatController {
    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatDTO.ChatResponse>> listChats() {
        return ResponseEntity.ok(chatService.getChatsForUser(getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<ChatDTO.ChatResponse> createChat(@RequestBody ChatDTO.CreateChatRequest request) {
        // Logic tìm chat riêng tư hoặc tạo mới nên nằm trọn trong Service để đảm bảo
        // tính nguyên tử (Atomicity)
        return ResponseEntity.ok(chatService.createChat(request, getCurrentUserId()));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDTO.ChatDetailResponse> getChat(@PathVariable UUID chatId) {
        return ResponseEntity.ok(chatService.getChat(chatId, getCurrentUserId()));
    }

    @PostMapping("/{chatId}/members")
    public ResponseEntity<ChatDTO.ChatMember> addMember(@PathVariable UUID chatId,
            @RequestBody ChatDTO.AddMemberRequest req) {
        return ResponseEntity.ok(chatService.addMember(chatId, req, getCurrentUserId()));
    }

    @DeleteMapping("/{chatId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID chatId, @PathVariable UUID memberId) {
        chatService.removeMember(chatId, memberId, getCurrentUserId());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{chatId}/leave")
    public ResponseEntity<Void> leaveChat(@PathVariable UUID chatId) {
        chatService.leaveChat(chatId, getCurrentUserId());
        return ResponseEntity.ok().build();
    }


    // Hàm lấy ID người dùng (Nên đưa vào một BaseController hoặc SecurityUtils)
    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserAuthenticate ua)
            return UUID.fromString(ua.getId());
        return UUID.fromString(principal.toString());
    }
}