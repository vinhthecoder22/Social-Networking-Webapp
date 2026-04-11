package com.example.socialnetworkingbackend.controller;

import com.example.socialnetworkingbackend.base.RestApiV1;
import com.example.socialnetworkingbackend.base.VsResponseUtil;
import com.example.socialnetworkingbackend.constant.UrlConstant;
import com.example.socialnetworkingbackend.domain.dto.request.RoleRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.RoleResponseDto;
import com.example.socialnetworkingbackend.exception.ForbiddenException;
import com.example.socialnetworkingbackend.security.CurrentUser;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestApiV1
@RequiredArgsConstructor
@Tag(name = "Role", description = "CRUD API for Role Management")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Create a new role (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(UrlConstant.Role.CREATE_ROLE)
    public ResponseEntity<?> createRole(
            @Valid @RequestBody RoleRequestDto requestDto) {
        RoleResponseDto responseDto = roleService.createRole(requestDto);
        return VsResponseUtil.success(responseDto);
    }

    @Operation(summary = "Get all roles (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(UrlConstant.Role.GET_ROLES)
    public ResponseEntity<?> getAllRoles() {
        List<RoleResponseDto> roles = roleService.getAllRoles();
        return VsResponseUtil.success(roles);
    }

    @GetMapping(UrlConstant.Role.GET_ROLE_BY_ID)
    public ResponseEntity<?> getRoleById(
            @PathVariable Long id,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {

        boolean isAdmin = currentUser.getRoleName().equalsIgnoreCase("ROLE_ADMIN");

        if (!isAdmin && !id.toString().equals(currentUser.getRoleId())) {
            throw new ForbiddenException("Bạn không có quyền truy cập role này");
        }

        RoleResponseDto role = roleService.getRoleById(id, currentUser);
        return VsResponseUtil.success(role);
    }

    @Operation(summary = "Update role (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(UrlConstant.Role.UPDATE_ROLE)
    public ResponseEntity<?> updateRole(@PathVariable Long id,
                                        @Valid @RequestBody RoleRequestDto requestDto) {
        RoleResponseDto updatedRole = roleService.updateRole(id, requestDto);
        return VsResponseUtil.success(updatedRole);
    }

    @Operation(summary = "Delete role (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(UrlConstant.Role.DELETE_ROLE)
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return VsResponseUtil.success("Role deleted successfully");
    }
}