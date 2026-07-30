package com.bk.arenax.identity.service;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("Account is temporarily locked");
    }
}
