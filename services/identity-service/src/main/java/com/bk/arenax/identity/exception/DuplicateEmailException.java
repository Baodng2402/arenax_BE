package com.bk.arenax.identity.exception;

public class DuplicateEmailException extends IdentityException {

    public DuplicateEmailException() {
        super("Email is already registered");
    }
}
