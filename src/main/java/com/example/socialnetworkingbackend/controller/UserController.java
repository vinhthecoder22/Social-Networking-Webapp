package com.example.socialnetworkingbackend.controller;

import com.example.socialnetworkingbackend.base.RestApiV1;
import com.example.socialnetworkingbackend.base.VsResponseUtil;
import com.example.socialnetworkingbackend.constant.UrlConstant;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationFullRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.ChangePasswordRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.UserCreateDto;
import com.example.socialnetworkingbackend.domain.dto.request.UserUpdateDto;
import com.example.socialnetworkingbackend.security.CurrentUser;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;

@RequiredArgsConstructor
@RestApiV1
@Tag(name = "User Controller", description = "Các chức năng liên quan tới user")
public class UserController {

    private final UserService userService;

    @GetMapping(UrlConstant.User.GET_USER)
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        return VsResponseUtil.success(userService.getUserById(userId));
    }

    @GetMapping(UrlConstant.User.GET_CURRENT_USER)
    public ResponseEntity<?> getCurrentUser(
            @Parameter(name = "principal", hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(userService.getCurrentUser(principal));
    }

    @PostMapping(UrlConstant.User.CREATE_USER)
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDto dto) {
        return VsResponseUtil.success(userService.createUser(dto));
    }

    @GetMapping(UrlConstant.User.GET_ALL_USERS)
    public ResponseEntity<?> getAllUsers(@Valid @ParameterObject PaginationFullRequestDto requestDTO) {
        return VsResponseUtil.success(userService.getAllUsers(requestDTO));
    }

    @PutMapping(UrlConstant.User.UPDATE_USER)
    public ResponseEntity<?> updateUser(@PathVariable String id,
                                        @Valid @RequestBody UserUpdateDto dto) {
        return VsResponseUtil.success(userService.updateUserName(id, dto));
    }

    @DeleteMapping(UrlConstant.User.DELETE_USER)
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return VsResponseUtil.success("User deleted successfully");
    }

    @PutMapping(UrlConstant.User.CHANGE_PASSWORD)
    public ResponseEntity<?> changePasswordUser(
            @Valid @RequestBody ChangePasswordRequestDto request,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {

        userService.changePassword(principal, request);
        return VsResponseUtil.success("Password changed successfully");
    }

}
