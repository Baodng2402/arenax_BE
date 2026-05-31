package com.bk.arenax.infrastructure.security;

import com.bk.arenax.domain.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
  private SecurityUtils() {}

  public static long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found in current request");
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof User user && user.getId() != null) {
      return user.getId();
    }

    throw new IllegalStateException("Current principal does not contain a valid user id");
  }
}
