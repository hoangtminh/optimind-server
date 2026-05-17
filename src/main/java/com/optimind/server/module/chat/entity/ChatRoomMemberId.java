package com.optimind.server.module.chat.entity;

import java.io.Serializable;

import com.optimind.server.module.user.entity.UserEntity;

import lombok.Data;

@Data
public class ChatRoomMemberId implements Serializable {
    private ChatRoomEnity chatRoom;
    private UserEntity member;
}
