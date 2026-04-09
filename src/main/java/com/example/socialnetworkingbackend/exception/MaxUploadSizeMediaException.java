package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class MaxUploadSizeMediaException extends RuntimeException {
    private HttpStatus status;
    private String[] params;

    public MaxUploadSizeMediaException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public MaxUploadSizeMediaException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public MaxUploadSizeMediaException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.params = params;
    }

    public MaxUploadSizeMediaException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.params = params;
    }
}