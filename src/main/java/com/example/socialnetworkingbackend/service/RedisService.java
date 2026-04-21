package com.example.socialnetworkingbackend.service;

import java.util.concurrent.TimeUnit;

public interface RedisService {

    void save(String key, String value);

    void save(String key, String value, long timeout, TimeUnit timeUnit);

    String get(String key);

    void delete(String key);

    boolean hasKey(String key);

    // Refresh token management
    void saveRefreshToken(String userId, String token, long timeout, TimeUnit unit);

    String getRefreshToken(String userId);

    void deleteRefreshToken(String userId);

    // Token blacklist
    boolean isBlacklisted(String token);

    void blacklistToken(String token, long timeout, TimeUnit unit);

}
