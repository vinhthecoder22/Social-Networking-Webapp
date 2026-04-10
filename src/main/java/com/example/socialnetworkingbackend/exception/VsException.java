package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class VsException extends RuntimeException {

    private HttpStatus status;
    private String[] params;

    public VsException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public VsException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public VsException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.params = params;
    }

    public VsException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.params = params;
    }
}