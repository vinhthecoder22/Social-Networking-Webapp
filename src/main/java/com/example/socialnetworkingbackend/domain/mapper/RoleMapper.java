package com.example.socialnetworkingbackend.domain.mapper;

import com.example.socialnetworkingbackend.domain.dto.request.RoleRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.RoleResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toEntity(RoleRequestDto dto);

    RoleResponseDto toRoleResponseDto(Role role);

}
