package com.bk.arenax.identity.exception;

public class InvalidCredentialsException extends IdentityException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
