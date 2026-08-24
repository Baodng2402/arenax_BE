package com.bk.arenax.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email @Size(max = 320) String email,
    @NotBlank @Size(min = 8, max = 128) String password,
    @NotBlank @Size(max = 120) String fullName) {

  public RegisterRequest {
    email = email == null ? null : email.trim();
    password = password == null ? null : password.trim();
    fullName = fullName == null ? null : fullName.trim();
  }
}
