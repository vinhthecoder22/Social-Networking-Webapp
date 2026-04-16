package com.example.socialnetworkingbackend.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);

        Map<String, Object> response = new HashMap<>();
        response.put("error", httpStatus.getReasonPhrase());
        response.put("message", "An error occurred while processing your request.");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(httpStatus).body(response);
    }
}