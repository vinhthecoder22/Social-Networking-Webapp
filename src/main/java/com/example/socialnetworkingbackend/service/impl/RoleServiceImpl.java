package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.RoleConstant;
import com.example.socialnetworkingbackend.domain.dto.request.RoleRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.RoleResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Role;
import com.example.socialnetworkingbackend.domain.mapper.RoleMapper;
import com.example.socialnetworkingbackend.exception.BadRequestException;
import com.example.socialnetworkingbackend.exception.ForbiddenException;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.exception.RoleAlreadyExistsException;
import com.example.socialnetworkingbackend.repository.RoleRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.RoleService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final UserRepository userRepository;

    @Override
    public RoleResponseDto createRole(RoleRequestDto request) {
        validateRoleName(request.getName());
        validateDuplicateRole(request.getName());

        Role role = roleMapper.toEntity(request);
        return roleMapper.toRoleResponseDto(roleRepository.save(role));
    }

    @Override
    public RoleResponseDto getRoleById(Long id, UserPrincipal currentUser) {
        boolean isAdmin = RoleConstant.ADMIN.equalsIgnoreCase(currentUser.getRoleName());

        if (!isAdmin && !Objects.equals(currentUser.getRoleId(), String.valueOf(id))) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN);
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.Role.ERR_NOT_FOUND,
                        new String[]{String.valueOf(id)}
                ));

        return roleMapper.toRoleResponseDto(role);
    }

    @Override
    public List<RoleResponseDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRoleResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponseDto updateRole(Long id, RoleRequestDto request) {
        validateRoleName(request.getName());

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.Role.ERR_NOT_FOUND,
                        new String[]{String.valueOf(id)}
                ));

        if (!role.getName().equals(request.getName())) {
            validateDuplicateRole(request.getName());
        }

        role.setName(request.getName());
        return roleMapper.toRoleResponseDto(roleRepository.save(role));
    }

    @Override
    public void deleteRole(Long id) {
        if (userRepository.existsByRole_Id(id)) {
            throw new BadRequestException(ErrorMessage.INVALID_SOME_THING_FIELD);
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.Role.ERR_NOT_FOUND,
                        new String[]{String.valueOf(id)}
                ));

        roleRepository.delete(role);
    }

    private void validateRoleName(String name) {
        if (!RoleConstant.ADMIN.equals(name) && !RoleConstant.USER.equals(name)) {
            throw new BadRequestException(ErrorMessage.INVALID_SOME_THING_FIELD);
        }
    }

    private void validateDuplicateRole(String roleName) {
        if (roleRepository.existsByName(roleName)) {
            throw new RoleAlreadyExistsException(roleName);
        }
    }
}