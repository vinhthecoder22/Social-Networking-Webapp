package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class InvalidException extends RuntimeException {
    private HttpStatus status;
    private String[] params;

    public InvalidException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public InvalidException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public InvalidException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.params = params;
    }

    public InvalidException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.params = params;
    }
}