package com.bk.arenax.adapter.service;

import com.bk.arenax.domain.rbac.Role;
import com.bk.arenax.domain.user.User;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RbacAuthorizationService {

  public void requirePermission(String module, String action) {
    requirePermission(null, module, action);
  }

  public void requirePermission(Long accountId, String module, String action) {
    User currentUser = currentUser();
    if (!hasPermission(currentUser, accountId, module, action)) {
      throw new AccessDeniedException("Missing permission: " + permissionCode(module, action));
    }
  }

  public void requireSelfOrPermission(Long userId, String module, String action) {
    User currentUser = currentUser();
    if (currentUser.getId() != null && currentUser.getId().equals(userId)) {
      return;
    }
    if (!hasPermission(currentUser, null, module, action)) {
      throw new AccessDeniedException("Missing permission: " + permissionCode(module, action));
    }
  }

  public boolean hasPermission(User user, Long accountId, String module, String action) {
    Set<String> permissions = permissionCodes(user, accountId);
    String required = permissionCode(module, action);
    String manage = permissionCode(module, "MANAGE");
    return permissions.contains(required) || permissions.contains(manage);
  }

  private User currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
      throw new AccessDeniedException("Authenticated user is required");
    }
    return user;
  }

  private Set<String> permissionCodes(User user, Long accountId) {
    Set<Role> roles = accountId == null ? user.roles() : user.rolesForAccount(accountId);
    return roles.stream()
        .filter(Role::isActive)
        .flatMap(role -> role.getPermissions().stream())
        .filter(permission -> permission.isActive())
        .map(permission -> permission.getCodeName())
        .collect(Collectors.toSet());
  }

  private String permissionCode(String module, String action) {
    return normalize(module) + "_" + normalize(action);
  }

  private String normalize(String value) {
    return value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
  }
}
