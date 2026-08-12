package com.bk.arenax.identity.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddEmailRequest(
        @NotBlank @Email @Size(max = 320) String email) {

    public AddEmailRequest {
        email = email == null ? null : email.trim();
    }
}
