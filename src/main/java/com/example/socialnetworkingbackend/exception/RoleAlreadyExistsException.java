package com.example.socialnetworkingbackend.exception;

import org.springframework.http.HttpStatus;

public class RoleAlreadyExistsException extends VsException {
    public RoleAlreadyExistsException(String name) {
        super(HttpStatus.CONFLICT, "error.role.name.already_exists", new String[]{name});
    }
}
