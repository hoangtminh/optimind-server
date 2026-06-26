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

    @PostMapping("/google")
    public ResponseEntity<?> handleGoogleLogin(@RequestBody AuthRequest.GoogleLoginRequest req) {
        if (req.idToken() != null && !req.idToken().trim().isEmpty()) {
            AuthResponse.AuthenticateResponse jwt = authService.processGoogleIdTokenLogin(
                    new AuthRequest.GoogleIdTokenRequest(req.idToken())
            );
            return ResponseEntity.ok(Map.of("token", jwt));
        } else if (req.code() != null && !req.code().trim().isEmpty()) {
            AuthResponse.AuthenticateResponse jwt = authService.processGoogleLogin(
                    new AuthRequest.OAuth2Request(req.code(), req.redirectUri())
            );
            return ResponseEntity.ok(Map.of("token", jwt));
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> handleGoogleCallback(
            @org.springframework.web.bind.annotation.RequestParam("code") String code,
            @org.springframework.web.bind.annotation.RequestParam("state") String state) {
        
        String delimiter = state.contains("?") ? "&" : "?";
        String redirectUrl = state + delimiter + "code=" + code;

        return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                .header(org.springframework.http.HttpHeaders.LOCATION, redirectUrl)
                .build();
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
