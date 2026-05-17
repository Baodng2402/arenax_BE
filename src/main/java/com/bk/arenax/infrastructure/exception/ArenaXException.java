package com.bk.arenax.infrastructure.exception;

import org.springframework.http.HttpStatus;

public abstract class ArenaXException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    protected ArenaXException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
