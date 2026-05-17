package com.optimind.server.module.auth.dto;

public class AuthResponse {
    public record RegisterResponse(String email, String username) {
    }

    public record AuthenticateResponse(String accessToken, String refreshToken) {
    }

}
