package com.optimind.server.module.friend.service;

import com.optimind.server.module.friend.dto.FriendDTO;
import com.optimind.server.module.friend.entity.FriendRequestEntity;
import com.optimind.server.module.friend.entity.FriendshipEntity;
import com.optimind.server.module.friend.repo.FriendRequestRepository;
import com.optimind.server.module.friend.repo.FriendshipRepository;
import com.optimind.server.module.user.entity.UserEntity;
import com.optimind.server.module.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;

    @Override
    @Transactional
    public FriendDTO.FriendRequestResponse sendFriendRequest(UUID senderId, String receiverEmail) {
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        UserEntity receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        if (friendshipRepository.findFriendshipBetween(sender.getId(), receiver.getId()).isPresent()) {
            throw new IllegalArgumentException("Users are already friends");
        }

        if (friendRequestRepository.findPendingRequest(sender.getId(), receiver.getId()).isPresent() ||
                friendRequestRepository.findPendingRequest(receiver.getId(), sender.getId()).isPresent()) {
            throw new IllegalArgumentException("Friend request already exists");
        }

        FriendRequestEntity request = FriendRequestEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .build();

        request = friendRequestRepository.save(request);

        return new FriendDTO.FriendRequestResponse(request.getId(), toUserSummary(receiver));
    }

    @Override
    @Transactional
    public FriendDTO.FriendResponse acceptFriendRequest(UUID receiverId, UUID requestId) {
        FriendRequestEntity request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new SecurityException("You are not authorized to accept this request");
        }

        UserEntity user1 = request.getSender();
        UserEntity user2 = request.getReceiver();

        if (friendshipRepository.findFriendshipBetween(user1.getId(), user2.getId()).isPresent()) {
            throw new IllegalArgumentException("Users are already friends");
        }
        FriendshipEntity friendship = FriendshipEntity.builder().user1(user1).user2(user2).build();
        friendship = friendshipRepository.save(friendship);
        friendRequestRepository.delete(request);

        return new FriendDTO.FriendResponse(friendship.getId(), toUserSummary(user1));
    }

    @Override
    @Transactional
    public void declineFriendRequest(UUID receiverId, UUID requestId) {
        FriendRequestEntity request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new SecurityException("You are not authorized to decline this request");
        }
        friendRequestRepository.delete(request);
    }

    @Override
    @Transactional
    public void withdrawFriendRequest(UUID senderId, UUID requestId) {
        FriendRequestEntity request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
        if (!request.getSender().getId().equals(senderId)) {
            throw new SecurityException("You are not authorized to withdraw this request");
        }
        friendRequestRepository.delete(request);
    }

    @Override
    @Transactional
    public void unfriend(UUID currentUserId, UUID friendId) {
        FriendshipEntity friendship = friendshipRepository.findFriendshipBetween(currentUserId, friendId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));
        friendshipRepository.delete(friendship);
    }

    @Override
    public List<FriendDTO.FriendResponse> getFriends(UUID userId) {
        return friendshipRepository.findAllByUserId(userId).stream()
                .map(f -> {
                    UserEntity friend = f.getUser1().getId().equals(userId) ? f.getUser2() : f.getUser1();
                    return new FriendDTO.FriendResponse(f.getId(), toUserSummary(friend));
                }).collect(Collectors.toList());
    }

    @Override
    public List<FriendDTO.FriendRequestResponse> getIncomingFriendRequests(UUID userId) {
        return friendRequestRepository.findByReceiverId(userId).stream()
                .map(req -> new FriendDTO.FriendRequestResponse(req.getId(), toUserSummary(req.getSender())))
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendDTO.FriendRequestResponse> getSentFriendRequests(UUID userId) {
        return friendRequestRepository.findBySenderId(userId).stream()
                .map(req -> new FriendDTO.FriendRequestResponse(req.getId(), toUserSummary(req.getReceiver())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FriendDTO.SearchFriendResult searchFriendByEmail(UUID currentUserId, String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        if (user.getId().equals(currentUserId)) {
            return new FriendDTO.SearchFriendResult(
                    user.getId(), user.getUsername(), user.getEmail(), user.getImageUrl(), "SELF");
        }

        // Check friendship
        if (friendshipRepository.findFriendshipBetween(currentUserId, user.getId()).isPresent()) {
            return new FriendDTO.SearchFriendResult(
                    user.getId(), user.getUsername(), user.getEmail(), user.getImageUrl(), "FRIEND");
        }

        // Check if current user sent a request to searched user
        if (friendRequestRepository.findPendingRequest(currentUserId, user.getId()).isPresent()) {
            return new FriendDTO.SearchFriendResult(
                    user.getId(), user.getUsername(), user.getEmail(), user.getImageUrl(), "REQUEST_SENT");
        }

        // Check if current user received a request from searched user
        if (friendRequestRepository.findPendingRequest(user.getId(), currentUserId).isPresent()) {
            return new FriendDTO.SearchFriendResult(
                    user.getId(), user.getUsername(), user.getEmail(), user.getImageUrl(), "REQUEST_RECEIVED");
        }

        // Otherwise
        return new FriendDTO.SearchFriendResult(
                user.getId(), user.getUsername(), user.getEmail(), user.getImageUrl(), "NONE");
    }

    private FriendDTO.UserSummary toUserSummary(UserEntity user) {
        return new FriendDTO.UserSummary(user.getId(), user.getUsername(), user.getEmail(), user.getImageUrl());
    }
}