package com.optimind.server.module.auth;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimind.server.module.auth.dto.AuthRequest;
import com.optimind.server.module.auth.dto.AuthResponse;
import com.optimind.server.module.user.dto.UserDto;
import com.optimind.server.module.user.entity.UserEntity;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthMapper authMapper;

    @PostMapping("/register")
    public UserEntity register(@RequestBody AuthRequest.RegisterRequest authReq) {
        UserEntity savedUser = authService.createUser(authReq);
        return savedUser;
    }

    @PostMapping("/login")
    public AuthResponse.AuthenticateResponse login(@RequestBody AuthRequest.LoginRequest authReq) {
        System.out.println(authReq);
        AuthResponse.AuthenticateResponse token = authService.verifyUser(authReq);
        return token;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody AuthRequest.LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Đăng xuất thành công");
    }

    @PostMapping("/refresh")
    public AuthResponse.AuthenticateResponse refresh(@RequestBody AuthRequest.RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/oauth2/google")
    public ResponseEntity<?> handleGoogleLogin(@RequestBody AuthRequest.OAuth2Request req) {
        String code = req.code();
        if (code == null)
            return ResponseEntity.badRequest().build();

        // Xử lý đổi code lấy thông tin user & tạo JWT (như hướng dẫn trước)
        AuthResponse.AuthenticateResponse jwt = authService.processGoogleLogin(req);

        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserAuthenticate) {
            UserAuthenticate userAuthenticate = (UserAuthenticate) authentication.getPrincipal();
            return ResponseEntity.ok(authMapper.mapToUserDto(userAuthenticate.getUser()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
