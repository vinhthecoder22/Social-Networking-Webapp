package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class BadRequestException extends RuntimeException {
    private HttpStatus status;
    private String[] params;

    public BadRequestException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BadRequestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public BadRequestException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.params = params;
    }

    public BadRequestException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.params = params;
    }
}