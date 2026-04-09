package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void save(String key, String value, long timeout, java.util.concurrent.TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key) == null ? null : redisTemplate.opsForValue().get(key).toString();
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

}
