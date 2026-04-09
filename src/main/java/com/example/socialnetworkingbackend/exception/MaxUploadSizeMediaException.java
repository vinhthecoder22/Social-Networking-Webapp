package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class MaxUploadSizeMediaException extends RuntimeException {

    private String message;

    private HttpStatus status;

    private String[] params;

    public MaxUploadSizeMediaException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.message = message;
    }

    public MaxUploadSizeMediaException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    public MaxUploadSizeMediaException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.message = message;
        this.params = params;
    }

    public MaxUploadSizeMediaException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.message = message;
        this.params = params;
    }
}
