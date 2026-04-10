package com.example.socialnetworkingbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class UploadFileException extends RuntimeException {
    private HttpStatus status;
    private String[] params;

    public UploadFileException(String message) {
        super(message);
        this.status = HttpStatus.BAD_GATEWAY;
    }

    public UploadFileException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public UploadFileException(String message, String[] params) {
        super(message);
        this.status = HttpStatus.BAD_GATEWAY;
        this.params = params;
    }

    public UploadFileException(HttpStatus status, String message, String[] params) {
        super(message);
        this.status = status;
        this.params = params;
    }
}