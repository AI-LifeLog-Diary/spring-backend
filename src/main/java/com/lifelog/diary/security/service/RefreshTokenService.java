package com.lifelog.diary.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    // Refresh Token Redis에 저장 (만료 시간 설정)
    public void storeRefreshToken(String username, String refreshToken, long expirationTimeMs) {
        redisTemplate.opsForValue().set(username, refreshToken, expirationTimeMs, TimeUnit.MILLISECONDS);
    }

    // Refresh Token Redis에서 가져오기
    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(username);
    }

    // Refresh Token Redis에서 삭제
    public void deleteRefreshToken(String username) {
        redisTemplate.delete(username);
    }

}

