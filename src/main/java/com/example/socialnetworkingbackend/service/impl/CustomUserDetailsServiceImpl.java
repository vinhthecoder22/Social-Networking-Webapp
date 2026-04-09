package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.CustomUserDetailsService;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService, CustomUserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
                                        new String[]{usernameOrEmail}
                                )
                        )
                );
        return UserPrincipal.create(user);
    }


    @Override
    @Transactional
    public UserDetails loadUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id}));
        return UserPrincipal.create(user);
    }

}
