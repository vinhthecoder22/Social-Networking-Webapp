package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class InternalServerException extends RuntimeException {
    private HttpStatus status;
    private String[] params;

    public InternalServerException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public InternalServerException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public InternalServerException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.params = params;
    }

    public InternalServerException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.params = params;
    }
}