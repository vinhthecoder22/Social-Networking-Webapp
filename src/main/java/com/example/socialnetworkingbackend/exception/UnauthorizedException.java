package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class UnauthorizedException extends RuntimeException {
    private HttpStatus status;
    private String[] params;

    public UnauthorizedException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
    }

    public UnauthorizedException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public UnauthorizedException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
        this.params = params;
    }

    public UnauthorizedException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.params = params;
    }
}