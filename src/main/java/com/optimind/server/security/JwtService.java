package com.optimind.server.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.optimind.server.module.user.entity.UserEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    public String generateToken(UserEntity userDetails, int expiredTime) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getId().toString());
        claims.put("email", userDetails.getEmail());
        claims.put("username", userDetails.getUsername());
        claims.put("role", userDetails.getRole());
        claims.put("imageUrl", userDetails.getImageUrl());

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(userDetails.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredTime))
                .and()
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(UserEntity user) {
        return generateToken(user, 1000 * 60 * 60 * 24 * 3);
    }

    public String generateAccessToken(UserEntity user) {
        return generateToken(user, 1000 * 60 * 60 * 3);
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Extract user email
    public String extractUserEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserEmail(token);

        return (userName.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && userDetails.isEnabled());
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
