package com.example.socialnetworkingbackend.service;

public interface RedisService {

    void save(String key, String value);

    void save(String key, String value, long timeout, java.util.concurrent.TimeUnit timeUnit);

    String get(String key);

    void delete(String key);

    boolean hasKey(String key);

}
