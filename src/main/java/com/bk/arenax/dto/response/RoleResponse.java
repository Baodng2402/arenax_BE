package com.bk.arenax.dto.response;

import com.bk.arenax.domain.rbac.Role;
import java.util.Set;
import java.util.stream.Collectors;

public record RoleResponse(
    Long id,
    String name,
    String codeName,
    String authority,
    String description,
    boolean systemRole,
    Set<String> permissionCodeNames) {
  public static RoleResponse from(Role role) {
    return new RoleResponse(
        role.getId(),
        role.getName(),
        role.getCodeName(),
        role.authorityName(),
        role.getDescription(),
        role.isSystemRole(),
        role.getPermissions().stream()
            .map(permission -> permission.getCodeName())
            .collect(Collectors.toSet()));
  }
}
