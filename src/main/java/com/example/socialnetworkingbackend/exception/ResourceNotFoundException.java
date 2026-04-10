package com.example.socialnetworkingbackend.exception;

public class ResourceNotFoundException extends NotFoundException {
    public ResourceNotFoundException(String resource, String field, String value) {
        super("error.resource.not_found", new String[]{resource, field, value});
    }
}