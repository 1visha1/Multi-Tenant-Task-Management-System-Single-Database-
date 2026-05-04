package com.task.management.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.management.io.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // common builder method
    private ResponseEntity<ErrorResponse> build(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // 400 - Bad Request
    @ExceptionHandler({
            PasswordMismatchException.class,
            TenantCreationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return build(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 401 - Unauthorized
    @ExceptionHandler({
            AuthenticationFailedException.class,
            UserNotAuthenticatedException.class
    })
    public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException ex) {
        return build(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // 403 - Forbidden
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(UnauthorizedActionException ex) {
        return build(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // 404 - Not Found
    @ExceptionHandler({
            UserNotFoundException.class,
            TaskNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return build(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 409 - Conflict
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(UserAlreadyExistsException ex) {
        return build(ex.getMessage(), HttpStatus.CONFLICT);
    }
    
    

    // 500 - Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}