package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.AuthProvider;
import com.example.socialnetworkingbackend.constant.RoleConstant;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.repository.RoleRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public UserPrincipal processUser(String registrationId, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        String email = extractEmail(registrationId, attributes);

        User user = userRepository.findByUsernameOrEmail(email, email).orElseGet(() -> {
            log.info("Creating new user from OAuth2: {}", email);
            return registerNewUser(registrationId, attributes, email);
        });

        return UserPrincipal.create(user, attributes, idToken, userInfo);
    }

    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        if (!StringUtils.hasText(email)) {
            String providerId = String.valueOf(attributes.get(registrationId.equals("google") ? "sub" : "id"));
            email = providerId + "@" + registrationId + ".com";
            log.warn("Email missing. Using fallback email: {}", email);
        }
        return email.toLowerCase();
    }

    private User registerNewUser(String registrationId, Map<String, Object> attributes, String email) {
        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword("");

        if (registrationId.equals("google")) {
            user.setProvider(AuthProvider.GOOGLE);
            user.setProviderId(String.valueOf(attributes.get("sub")));
            user.setFirstName(String.valueOf(attributes.getOrDefault("given_name", "")));
            user.setLastName(String.valueOf(attributes.get("family_name")));
            user.setImageUrl(String.valueOf(attributes.get("picture")));
        } else {
            user.setProvider(AuthProvider.FACEBOOK);
            user.setProviderId(String.valueOf(attributes.get("id")));
            user.setFirstName(String.valueOf(attributes.get("first_name")));
            user.setLastName(String.valueOf(attributes.get("last_name")));
        }

        user.setDob(LocalDate.of(1970, 1, 1));
        user.setRole(roleRepository.findByName(RoleConstant.USER).orElseThrow());
        return userRepository.save(user);
    }
}