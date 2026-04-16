package com.example.socialnetworkingbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // Bất đồng bộ
public class SocialNetworkingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialNetworkingBackendApplication.class, args);
    }

}
