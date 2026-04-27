package com.example.socialnetworkingbackend;

import com.cloudinary.Cloudinary;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class SocialNetworkingBackendApplicationTests {

    @MockitoBean
    private Cloudinary cloudinary;

    @MockitoBean
    private FirebaseApp firebaseApp;

    @MockitoBean
    private FirebaseAuth firebaseAuth;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
    }
}
