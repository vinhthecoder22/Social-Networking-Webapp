package com.example.socialnetworkingbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private final String API_KEY = "Bearer Token";

  @Bean
  public OpenAPI customOpenAPI() {
      return new OpenAPI()
          .info(new Info()
              .title("Social Networking API")
              .version("1.0")
              .description("Tài liệu API cho mạng xã hội Social-Networking-Webapp"))
          .components(new Components()
              .addSecuritySchemes(API_KEY, new SecurityScheme()
                  .scheme("Bearer")
                  .bearerFormat("JWT")
                  .type(SecurityScheme.Type.HTTP)))
          .addSecurityItem(new SecurityRequirement().addList(API_KEY));
  }
}