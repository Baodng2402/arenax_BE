package com.bk.arenax.infrastructure.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ArenaXException {
    public ResourceNotFoundException(String code, String message) {
        super(code, message, HttpStatus.NOT_FOUND);
    }
}
