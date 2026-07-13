package com.bk.arenax.identity.exception;

public class UserOnboardingIncompleteException extends IdentityException {

    public UserOnboardingIncompleteException() {
        super("User onboarding is not complete");
    }
}
