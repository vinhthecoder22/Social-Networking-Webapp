package com.example.socialnetworkingbackend.base;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class VsResponseUtil {

    public static ResponseEntity<RestData<?>> successWithMessage(String message) {
        RestData<?> response = RestData.successWithMessage(message);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public static ResponseEntity<RestData<?>> success(Object data) {
        return success(HttpStatus.OK, data);
    }

    public static ResponseEntity<RestData<?>> success(HttpStatus status, Object data) {
        RestData<?> response = new RestData<>(data);
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<RestData<?>> success(MultiValueMap<String, String> header, Object data) {
        return success(HttpStatus.OK, header, data);
    }

    public static ResponseEntity<RestData<?>> success(HttpStatus status, MultiValueMap<String, String> header, Object data) {
        RestData<?> response = new RestData<>(data);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.addAll(header);
        return ResponseEntity.ok().headers(responseHeaders).body(response);
    }

    public static ResponseEntity<RestData<?>> error(HttpStatus status, String message) {
        RestData<?> response = RestData.error(status.name(), message);
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<RestData<?>> error(HttpStatus status, String code, String message) {
        RestData<?> response = RestData.error(code, message);
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<RestData<?>> errorValidation(HttpStatus status, Object data) {
        RestData<?> response = RestData.errorData(status.name(), "Validation Error", data);
        return new ResponseEntity<>(response, status);
    }

}

