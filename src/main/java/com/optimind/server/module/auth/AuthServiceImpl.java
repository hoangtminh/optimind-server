package com.optimind.server.module.auth;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.optimind.server.module.auth.dto.AuthRequest;
import com.optimind.server.module.auth.dto.AuthResponse;
import com.optimind.server.module.auth.dto.AuthResponse.AuthenticateResponse;
import com.optimind.server.module.user.entity.UserEntity;
import com.optimind.server.module.user.repo.UserRepository;
import com.optimind.server.security.JwtService;
import com.optimind.server.security.TokenEntity;
import com.optimind.server.security.TokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final TokenRepository tokenRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    @Override
    public UserEntity createUser(AuthRequest.RegisterRequest authReq) {

        if (userRepository.findByEmail(authReq.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        UserEntity authEntity = authMapper.mapRegisterToUserEntity(authReq);

        final String password = authEntity.getPassword();
        authEntity.setPassword(passwordEncoder.encode(password));
        authEntity.setRole(UserEntity.Role.USER.toString());

        return userRepository.save(authEntity);
    }

    @Override
    public AuthResponse.AuthenticateResponse verifyUser(AuthRequest.LoginRequest loginEntity) {
        Authentication auth = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginEntity.email(), loginEntity.password()));

        UserEntity user = userRepository.findByEmail(loginEntity.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = null;

        if (loginEntity.remember()) {
            refreshToken = jwtService.generateRefreshToken(user);
            saveUserToken(user, refreshToken);
        }

        if (auth.isAuthenticated()) {
            return new AuthenticateResponse(accessToken, refreshToken);
        } else {
            return null;
        }
    }

    @Override
    public void logout(AuthRequest.LogoutRequest request) {
        if (request.refreshToken() != null) {
            tokenRepository.findByRefreshToken(request.refreshToken())
                    .ifPresent(token -> tokenRepository.delete(token));
        }
    }

    @Override
    public AuthResponse.AuthenticateResponse processGoogleLogin(AuthRequest.OAuth2Request req) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", req.code());
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        
        String redirectUri = req.redirectUri();
        if (redirectUri == null || redirectUri.trim().isEmpty()) {
            redirectUri = "http://localhost:3000/auth/callback";
        }
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, tokenHeaders);
        var response = restTemplate.postForEntity(GOOGLE_TOKEN_URL, tokenRequest, Map.class);

        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            throw new RuntimeException("Không lấy được access token từ Google");
        }

        String googleAccessToken = (String) response.getBody().get("access_token");

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(googleAccessToken);
        HttpEntity<String> userEntity = new HttpEntity<>(userHeaders);

        var userResponse = restTemplate.exchange(GOOGLE_USER_INFO_URL, HttpMethod.GET, userEntity, Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = (Map<String, Object>) userResponse.getBody();

        if (userInfo == null || userInfo.get("email") == null) {
            throw new RuntimeException("Không lấy được thông tin người dùng từ Google");
        }

        String email = (String) userInfo.get("email");
        UserEntity user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.setImageUrl((String) userInfo.get("picture"));
                    existingUser.setUsername((String) userInfo.get("name"));
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(email);
                    newUser.setUsername((String) userInfo.get("name"));
                    newUser.setImageUrl((String) userInfo.get("picture"));
                    newUser.setRole(UserEntity.Role.USER.toString());
                    return userRepository.save(newUser);
                });

        String refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, refreshToken);

        AuthResponse.AuthenticateResponse res = new AuthenticateResponse(
                jwtService.generateAccessToken(user),
                refreshToken);
        return res;

    }

    @Override
    public AuthResponse.AuthenticateResponse refreshToken(AuthRequest.RefreshTokenRequest request) {
        return tokenRepository.findByRefreshToken(request.refreshToken())
                .map(token -> {
                    if (token.getExpiryDate().isBefore(Instant.now())) {
                        tokenRepository.delete(token);
                        throw new RuntimeException("Refresh token was expired. Please make a new signin request");
                    }
                    return token;
                })
                .map(token -> {
                    String accessToken = jwtService.generateAccessToken(token.getUser());
                    return new AuthResponse.AuthenticateResponse(accessToken, request.refreshToken());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    private void saveUserToken(UserEntity user, String refreshToken) {
        TokenEntity token = new TokenEntity();
        token.setUser(user);
        token.setRefreshToken(refreshToken);
        token.setRevoked(false);
        token.setExpiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 3)); // 3 days
        tokenRepository.save(token);
    }
}