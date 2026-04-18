package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.AuthProvider;
import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.RoleConstant;
import com.example.socialnetworkingbackend.domain.dto.request.FirebaseLoginRequest;
import com.example.socialnetworkingbackend.domain.dto.response.LoginResponseDto;

import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.exception.UnauthorizedException;
import com.example.socialnetworkingbackend.repository.RoleRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.security.jwt.JwtTokenProvider;
import com.example.socialnetworkingbackend.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class FirebaseAuthService {

   private final FirebaseAuth firebaseAuth;
   private final UserRepository userRepository;
   private final RoleRepository roleRepository;
   private final JwtTokenProvider jwtTokenProvider;
   private final RedisService redisService;
   private final ObjectMapper objectMapper;

   @Transactional
   public LoginResponseDto loginWithFirebase(FirebaseLoginRequest request, HttpServletRequest httpRequest) {
      try {
         // 1. Verify ID Token using Firebase Admin SDK
         FirebaseToken decodedToken = firebaseAuth.verifyIdToken(request.getIdToken());
         String uid = decodedToken.getUid();
         String email = decodedToken.getEmail();
         String name = decodedToken.getName();
         String picture = decodedToken.getPicture();

         log.info("Firebase Auth: Verified token for email: {}", email);

         // 2. Find or Create User
         User user = userRepository.findByUsernameOrEmail(email, email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(email);
            newUser.setPassword("");

             // Safely split name into firstName and lastName
             if (name != null && name.contains(" ")) {
                 int lastSpace = name.lastIndexOf(' ');
                 newUser.setFirstName(name.substring(0, lastSpace));
                 newUser.setLastName(name.substring(lastSpace + 1));
             } else {
                 newUser.setFirstName(name != null ? name : "User");
                 newUser.setLastName("");
             }
             newUser.setDob(LocalDate.of(2000, 1, 1)); // Default dob for OAuth users

            newUser.setImageUrl(picture);
            newUser.setProvider(AuthProvider.GOOGLE);
            newUser.setProviderId(uid);
            newUser.setRole(roleRepository.findByName(RoleConstant.USER)
                  .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND)));
            return userRepository.save(newUser);
         });

         // 3. Create App Tokens (Access/Refresh)
         UserPrincipal userPrincipal = UserPrincipal.create(user);
         Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null,
               userPrincipal.getAuthorities());
         SecurityContextHolder.getContext().setAuthentication(authentication);

         String accessToken = jwtTokenProvider.generateToken(userPrincipal, false);
         String refreshToken = jwtTokenProvider.generateToken(userPrincipal, true);

         createOrUpdateSession(userPrincipal.getUsername());

         return new LoginResponseDto(accessToken, refreshToken, userPrincipal.getId(), authentication.getAuthorities());

      } catch (FirebaseAuthException e) {
          log.error("Firebase Token Verification Failed", e);
          throw new UnauthorizedException("Invalid Firebase ID Token");
      } catch (Exception e) {
          log.error("Login with Firebase failed", e);
          throw new com.example.socialnetworkingbackend.exception.InternalServerException(ErrorMessage.ERR_EXCEPTION_GENERAL);
      }
   }

   private void createOrUpdateSession(String username) {
      // Just update Redis for "Online" status, no DB persistence
      try {
         Map<String, Object> dataSession = new HashMap<>();
         dataSession.put("username", username);
         dataSession.put("status", "ONLINE");
         dataSession.put("last_activity", LocalDateTime.now().toString());
         String json = objectMapper.writeValueAsString(dataSession);
         redisService.save("username:" + username + ":session", json);
      } catch (JsonProcessingException e) {
         log.error("Error saving session to Redis", e);
      }
   }
}
