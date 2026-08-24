package com.bk.arenax.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUsernameRequest(@NotBlank @Size(min = 3, max = 40) String username) {

  public UpdateUsernameRequest {
    username = username == null ? null : username.trim();
  }
}
