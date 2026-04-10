package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ConflictException extends RuntimeException {
    private HttpStatus status;
    private String[] params;

    public ConflictException(String message) {
        super(message);
        this.status = HttpStatus.CONFLICT;
    }

    public ConflictException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ConflictException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.CONFLICT;
        this.params = params;
    }

    public ConflictException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.params = params;
    }
}