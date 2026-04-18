package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.RoleConstant;
import com.example.socialnetworkingbackend.constant.SortByDataConstant;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationFullRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationResponseDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PagingMeta;
import com.example.socialnetworkingbackend.domain.dto.request.ChangePasswordRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.UserCreateDto;
import com.example.socialnetworkingbackend.domain.dto.request.UserUpdateDto;
import com.example.socialnetworkingbackend.domain.dto.response.UserResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Role;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.mapper.UserMapper;
import com.example.socialnetworkingbackend.exception.BadRequestException;
import com.example.socialnetworkingbackend.exception.ConflictException;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.RoleRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.UserService;
import com.example.socialnetworkingbackend.service.RedisService;
import com.example.socialnetworkingbackend.security.jwt.JwtTokenProvider;
import com.example.socialnetworkingbackend.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final RedisService redisService;

    private final JwtTokenProvider jwtTokenProvider;

    @PreAuthorize("isAuthenticated() or hasRole('ADMIN')")
    @Override
    public UserResponseDto getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[] { userId }));
        return userMapper.toUserDto(user);
    }

    @PreAuthorize("isAuthenticated() or hasRole('ADMIN')")
    @Override
    public UserResponseDto getCurrentUser(UserPrincipal principal) {
        User user = userRepository.getUser(principal);
        return userMapper.toUserDto(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new ConflictException(ErrorMessage.Auth.ERR_ALREADY_EXISTS_USERNAME);
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException(ErrorMessage.Auth.ERR_ALREADY_EXISTS_EMAIL);
        }

        User user = userMapper.toUser(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        Role role = roleRepository.findByName(RoleConstant.USER)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND,
                        new String[] { RoleConstant.USER }));
        user.setRole(role);
        return userMapper.toUserDto(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public PaginationResponseDto<UserResponseDto> getAllUsers(PaginationFullRequestDto request) {
        Pageable pageable = PaginationUtil.buildPageable(request, SortByDataConstant.USER);

        Page<User> pageUser = userRepository.findAll(pageable);

        List<UserResponseDto> userResponseDtos = pageUser.getContent().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());

        String sortBy = "";
        String sortType = "";

        if (pageUser.getSort().isSorted()) {
            Sort.Order order = pageUser.getSort().iterator().next();
            sortBy = order.getProperty();
            sortType = order.getDirection().name().toLowerCase();
        } else {
            sortBy = "id";
            sortType = "asc";
        }

        PagingMeta meta = new PagingMeta(
                pageUser.getTotalElements(),
                pageUser.getTotalPages(),
                pageUser.getNumber(),
                pageUser.getSize(),
                sortBy,
                sortType);

        return new PaginationResponseDto<>(meta, userResponseDtos);

    }

    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @Override
    public UserResponseDto updateUserName(String id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[] { id }));

        userMapper.updateUserFromDto(dto, user);

        user = userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[] { id }));
        userRepository.delete(user);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public void changePassword(UserPrincipal principal, ChangePasswordRequestDto request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.User.ERR_NOT_FOUND_ID,
                        new String[]{principal.getId()}
                ));


        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_OLD_PASSWORD_INCORRECT);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}
