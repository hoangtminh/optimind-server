package com.optimind.server.module.friend.dto;

import java.util.UUID;

public class FriendDTO {
    public record UserSummary(UUID id, String username, String email, String imageUrl) {
    }

    public record SearchFriendResult(
            UUID id,
            String username,
            String email,
            String imageUrl,
            String relationStatus // "FRIEND", "REQUEST_SENT", "REQUEST_RECEIVED", "NONE", "SELF"
    ) {}

    public record SendFriendRequest(String email) {
    }

    public record FriendRequestResponse(UUID id, UserSummary user) {
    }

    public record FriendResponse(UUID friendshipId, UserSummary friend) {
    }
}