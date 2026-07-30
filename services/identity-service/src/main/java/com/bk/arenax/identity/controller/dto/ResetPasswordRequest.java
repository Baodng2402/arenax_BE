package com.bk.arenax.identity.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(min = 32) String token,
        @NotBlank @Size(min = 8, max = 128) String newPassword) {

    public ResetPasswordRequest {
        token = token == null ? null : token.trim();
        newPassword = newPassword == null ? null : newPassword.trim();
    }
}
