package com.optimind.server.module.auth.dto;

public class AuthRequest {
        public record LoginRequest(String email, String password, boolean remember) {
        }

        public record RegisterRequest(String email, String password, String username) {
        }

        public record OAuth2Request(String code, String redirectUri) {
        }

        public record LogoutRequest(String refreshToken) {
        }

        public record RefreshTokenRequest(String refreshToken) {
        }

}
