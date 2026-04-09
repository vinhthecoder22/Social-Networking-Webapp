package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.domain.dto.request.RoleRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.RoleResponseDto;
import com.example.socialnetworkingbackend.security.UserPrincipal;

import java.util.List;

public interface RoleService {
    RoleResponseDto createRole(RoleRequestDto request);
    RoleResponseDto getRoleById(Long id, UserPrincipal currentUser);
    List<RoleResponseDto> getAllRoles();
    RoleResponseDto updateRole(Long id, RoleRequestDto request);
    void deleteRole(Long id);
}
