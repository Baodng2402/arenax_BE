package com.bk.arenax.identity.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(@NotBlank String token) {

  public VerifyEmailRequest {
    token = token == null ? null : token.trim();
  }
}
