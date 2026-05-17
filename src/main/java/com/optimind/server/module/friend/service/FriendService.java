package com.optimind.server.module.friend.service;

import com.optimind.server.module.friend.dto.FriendDTO;

import java.util.List;
import java.util.UUID;

public interface FriendService {
    FriendDTO.FriendRequestResponse sendFriendRequest(UUID senderId, String receiverEmail);

    FriendDTO.FriendResponse acceptFriendRequest(UUID receiverId, UUID requestId);

    void declineFriendRequest(UUID receiverId, UUID requestId);

    void withdrawFriendRequest(UUID senderId, UUID requestId);

    void unfriend(UUID currentUserId, UUID friendId);

    List<FriendDTO.FriendResponse> getFriends(UUID userId);

    List<FriendDTO.FriendRequestResponse> getIncomingFriendRequests(UUID userId);

    List<FriendDTO.FriendRequestResponse> getSentFriendRequests(UUID userId);
}