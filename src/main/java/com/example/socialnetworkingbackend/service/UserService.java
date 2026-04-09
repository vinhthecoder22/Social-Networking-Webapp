package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationFullRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationResponseDto;
import com.example.socialnetworkingbackend.domain.dto.request.ChangePasswordRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.UserCreateDto;
import com.example.socialnetworkingbackend.domain.dto.request.UserUpdateDto;
import com.example.socialnetworkingbackend.domain.dto.response.UserResponseDto;
import com.example.socialnetworkingbackend.security.UserPrincipal;

public interface UserService {

    UserResponseDto getUserById(String userId);

    UserResponseDto getCurrentUser(UserPrincipal principal);

    UserResponseDto createUser(UserCreateDto dto);

    PaginationResponseDto<UserResponseDto> getAllUsers(PaginationFullRequestDto request);

    UserResponseDto updateUserName(String id, UserUpdateDto dto);

    void deleteUser(String id);

    void changePassword(UserPrincipal principal, ChangePasswordRequestDto dto);


}
