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
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

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

    @Value("${google.client.android-id:}")
    private String androidClientId;

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
            refreshToken = getOrSaveUserToken(user);
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

        Map<String, Object> responseBody;
        try {
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, tokenHeaders);
            var response = restTemplate.postForEntity(GOOGLE_TOKEN_URL, tokenRequest, Map.class);
            responseBody = response.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("Lỗi khi trao đổi token với Google: " + e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi trao đổi token Google: " + e.getResponseBodyAsString(), e);
        }

        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new RuntimeException("Không lấy được access token từ Google");
        }

        String googleAccessToken = (String) responseBody.get("access_token");

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

        String refreshToken = getOrSaveUserToken(user);

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

    private String getOrSaveUserToken(UserEntity user) {
        List<TokenEntity> tokens = tokenRepository.findAllByUser(user);
        if (tokens.isEmpty()) {
            String newRefreshToken = jwtService.generateRefreshToken(user);
            TokenEntity token = new TokenEntity();
            token.setUser(user);
            token.setRefreshToken(newRefreshToken);
            token.setRevoked(false);
            token.setExpiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 3)); // 3 days
            tokenRepository.save(token);
            return newRefreshToken;
        }

        TokenEntity existingToken = tokens.get(0);

        if (tokens.size() > 1) {
            for (int i = 1; i < tokens.size(); i++) {
                tokenRepository.delete(tokens.get(i));
            }
        }

        if (existingToken.getExpiryDate().isAfter(Instant.now())) {
            return existingToken.getRefreshToken();
        } else {
            String newRefreshToken = jwtService.generateRefreshToken(user);
            existingToken.setRefreshToken(newRefreshToken);
            existingToken.setRevoked(false);
            existingToken.setExpiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 3)); // 3 days
            tokenRepository.save(existingToken);
            return newRefreshToken;
        }
    }

    @Override
    public AuthResponse.AuthenticateResponse processGoogleIdTokenLogin(AuthRequest.GoogleIdTokenRequest req) {
        try {
            List<String> audiences = new ArrayList<>();
            audiences.add(clientId);
            if (androidClientId != null && !androidClientId.trim().isEmpty()) {
                audiences.add(androidClientId);
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(audiences)
                    .build();

            GoogleIdToken idToken = verifier.verify(req.idToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");

                UserEntity user = userRepository.findByEmail(email)
                        .map(existingUser -> {
                            existingUser.setImageUrl(pictureUrl);
                            existingUser.setUsername(name);
                            return userRepository.save(existingUser);
                        })
                        .orElseGet(() -> {
                            UserEntity newUser = new UserEntity();
                            newUser.setEmail(email);
                            newUser.setUsername(name);
                            newUser.setImageUrl(pictureUrl);
                            newUser.setRole(UserEntity.Role.USER.toString());
                            return userRepository.save(newUser);
                        });

                String refreshToken = getOrSaveUserToken(user);

                return new AuthenticateResponse(
                        jwtService.generateAccessToken(user),
                        refreshToken);
            } else {
                throw new RuntimeException("Xác thực ID Token Google thất bại.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực Google ID Token: " + e.getMessage(), e);
        }
    }
}