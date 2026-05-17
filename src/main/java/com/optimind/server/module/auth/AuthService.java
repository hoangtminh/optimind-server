package com.optimind.server.module.auth;

import com.optimind.server.module.auth.dto.AuthRequest;
import com.optimind.server.module.auth.dto.AuthResponse;
import com.optimind.server.module.user.entity.UserEntity;

public interface AuthService {

    UserEntity createUser(AuthRequest.RegisterRequest authEntity);

    AuthResponse.AuthenticateResponse verifyUser(AuthRequest.LoginRequest authEntity);

    void logout(AuthRequest.LogoutRequest request);

    AuthResponse.AuthenticateResponse refreshToken(AuthRequest.RefreshTokenRequest request);

    AuthResponse.AuthenticateResponse processGoogleLogin(AuthRequest.OAuth2Request code);
}
