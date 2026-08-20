package com.bk.arenax.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record LoginRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(max = 128) String password,
        UUID accountId) {

    public LoginRequest {
        email = email == null ? null : email.trim();
        password = password == null ? null : password.trim();
    }
}
