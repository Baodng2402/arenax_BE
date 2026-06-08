package com.bk.arenax.dto.response;

import com.bk.arenax.domain.user.User;
import java.util.Set;

public record AuthUserResponse(
    Long id, String email, String name, Set<String> roles, Set<String> permissions) {
  public static AuthUserResponse from(User user, Set<String> roles, Set<String> permissions) {
    return new AuthUserResponse(user.getId(), user.getEmail(), user.getName(), roles, permissions);
  }
}
