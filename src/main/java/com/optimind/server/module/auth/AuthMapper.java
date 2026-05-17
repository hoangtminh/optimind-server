package com.optimind.server.module.auth;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import com.optimind.server.module.auth.dto.AuthRequest;
import com.optimind.server.module.user.dto.UserDto;
import com.optimind.server.module.user.entity.UserEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {
    AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

    public UserDto mapToUserDto(UserEntity userEntity);

    public UserEntity mapRegisterToUserEntity(AuthRequest.RegisterRequest req);

    public UserEntity mapLoginToUserEntity(AuthRequest.LoginRequest req);
}
