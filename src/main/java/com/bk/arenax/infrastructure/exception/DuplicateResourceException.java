package com.bk.arenax.infrastructure.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ArenaXException {
    public DuplicateResourceException(String code, String message) {
        super(code, message, HttpStatus.CONFLICT);
    }
}
