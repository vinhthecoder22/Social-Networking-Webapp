package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ForbiddenException extends RuntimeException {
    private HttpStatus status;
    private String[] params;

    public ForbiddenException(String message) {
        super(message);
        this.status = HttpStatus.FORBIDDEN;
    }

    public ForbiddenException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ForbiddenException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.FORBIDDEN;
        this.params = params;
    }

    public ForbiddenException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.params = params;
    }
}