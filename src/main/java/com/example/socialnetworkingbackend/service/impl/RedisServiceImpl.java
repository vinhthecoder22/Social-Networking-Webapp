package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void save(String key, String value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    @Override
    public String get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : value.toString();
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void saveRefreshToken(String userId, String token, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set("refresh:user:" + userId, token, timeout, unit);
    }

    @Override
    public String getRefreshToken(String userId) {
        Object value = redisTemplate.opsForValue().get("refresh:user:" + userId);
        return value == null ? null : value.toString();
    }

    @Override
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete("refresh:user:" + userId);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }

    @Override
    public void blacklistToken(String token, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set("blacklist:" + token, "revoked", timeout, unit);
    }
}
