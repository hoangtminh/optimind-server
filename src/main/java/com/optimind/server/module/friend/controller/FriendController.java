package com.optimind.server.module.friend.controller;

import com.optimind.server.module.auth.UserAuthenticate;
import com.optimind.server.module.friend.dto.FriendDTO;
import com.optimind.server.module.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FriendController {

    private final FriendService friendService;

    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserAuthenticate ua) {
            return UUID.fromString(ua.getId());
        }
        return UUID.fromString(principal.toString());
    }

    @PostMapping("/requests")
    public ResponseEntity<FriendDTO.FriendRequestResponse> sendFriendRequest(
            @RequestBody FriendDTO.SendFriendRequest request) {
        FriendDTO.FriendRequestResponse response = friendService.sendFriendRequest(getCurrentUserId(), request.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<FriendDTO.FriendResponse> acceptFriendRequest(@PathVariable UUID requestId) {
        FriendDTO.FriendResponse response = friendService.acceptFriendRequest(getCurrentUserId(), requestId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/requests/{requestId}/decline")
    public ResponseEntity<Void> declineFriendRequest(@PathVariable UUID requestId) {
        friendService.declineFriendRequest(getCurrentUserId(), requestId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/requests/{requestId}/withdraw")
    public ResponseEntity<Void> withdrawFriendRequest(@PathVariable UUID requestId) {
        friendService.withdrawFriendRequest(getCurrentUserId(), requestId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> unfriend(@PathVariable UUID friendId) {
        friendService.unfriend(getCurrentUserId(), friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FriendDTO.FriendResponse>> getFriends() {
        return ResponseEntity.ok(friendService.getFriends(getCurrentUserId()));
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendDTO.FriendRequestResponse>> getIncomingFriendRequests() {
        return ResponseEntity.ok(friendService.getIncomingFriendRequests(getCurrentUserId()));
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<FriendDTO.FriendRequestResponse>> getSentFriendRequests() {
        return ResponseEntity.ok(friendService.getSentFriendRequests(getCurrentUserId()));
    }

    @GetMapping("/search")
    public ResponseEntity<FriendDTO.SearchFriendResult> searchFriendByEmail(@RequestParam String email) {
        FriendDTO.SearchFriendResult result = friendService.searchFriendByEmail(getCurrentUserId(), email);
        return ResponseEntity.ok(result);
    }
}