package com.bk.arenax.identity.controller;

import com.bk.arenax.identity.controller.dto.ErrorResponse;
import com.bk.arenax.identity.service.AccountLockedException;
import com.bk.arenax.identity.service.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("UNAUTHORIZED", exception.getMessage()));
    }

    @ExceptionHandler(AccountLockedException.class)
    ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException exception) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(new ErrorResponse("ACCOUNT_LOCKED", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("BAD_REQUEST", "Invalid request content."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("BAD_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ErrorResponse> handleGone(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new ErrorResponse("TOKEN_NO_LONGER_VALID", exception.getMessage()));
    }
}
