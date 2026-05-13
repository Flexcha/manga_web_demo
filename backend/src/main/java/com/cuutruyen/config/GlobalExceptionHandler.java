package com.cuutruyen.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        e.printStackTrace();
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = e.toString();
        }
        return ResponseEntity.status(500).body(Map.of(
            "error", "Internal Server Error",
            "message", message,
            "type", e.getClass().getName()
        ));
    }
}
