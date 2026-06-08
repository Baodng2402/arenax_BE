package com.bk.arenax.dto.response;

import com.bk.arenax.domain.rbac.Permission;

public record PermissionResponse(
    Long id, String name, String codeName, String module, String action, String description) {
  public static PermissionResponse from(Permission permission) {
    return new PermissionResponse(
        permission.getId(),
        permission.getName(),
        permission.getCodeName(),
        permission.getModule(),
        permission.getAction(),
        permission.getDescription());
  }
}
