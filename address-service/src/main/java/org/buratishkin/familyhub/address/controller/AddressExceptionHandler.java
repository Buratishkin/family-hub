package org.buratishkin.familyhub.address.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class AddressExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        String field = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField())
                .orElse("request");
        return badRequest("missing or invalid field: " + field);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody() {
        return badRequest("invalid json body");
    }

    private ResponseEntity<Map<String, Object>> badRequest(String reason) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("result", false);
        body.put("reason", reason);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
