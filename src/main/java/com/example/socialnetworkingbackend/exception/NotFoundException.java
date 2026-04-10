package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class NotFoundException extends RuntimeException {
    private HttpStatus status;
    private String[] params;

    public NotFoundException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }

    public NotFoundException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public NotFoundException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
        this.params = params;
    }

    public NotFoundException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.params = params;
    }
}