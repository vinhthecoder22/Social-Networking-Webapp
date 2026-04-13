package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.security.UserPrincipal;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import java.util.Map;

public interface OAuth2Service {
    UserPrincipal processUser(String registrationId, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo);
}