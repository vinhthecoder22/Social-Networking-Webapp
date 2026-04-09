package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
@Getter
@Setter
public class ConflictException extends RuntimeException {
    private String message;

    private HttpStatus status;

    private String[] params;

    public ConflictException(String message) {
        super(message);
        this.status = HttpStatus.CONFLICT;
        this.message = message;
    }

    public ConflictException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    public ConflictException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.CONFLICT;
        this.message = message;
        this.params = params;
    }

    public ConflictException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.message = message;
        this.params = params;
    }
}
