package com.optimind.server.security;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optimind.server.module.user.entity.UserEntity;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, Long> {
    Optional<TokenEntity> findByRefreshToken(String refreshToken);
    List<TokenEntity> findAllByUser(UserEntity user);
}
