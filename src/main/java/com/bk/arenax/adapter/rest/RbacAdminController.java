package com.bk.arenax.adapter.rest;

import com.bk.arenax.adapter.service.RbacService;
import com.bk.arenax.dto.request.CreateRoleRequest;
import com.bk.arenax.dto.request.RolePermissionsRequest;
import com.bk.arenax.dto.request.UserRolesRequest;
import com.bk.arenax.dto.response.ApiResponse;
import com.bk.arenax.dto.response.PermissionResponse;
import com.bk.arenax.dto.response.RoleResponse;
import com.bk.arenax.dto.response.UserRolesResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/rbac")
@RequiredArgsConstructor
public class RbacAdminController {
  private final RbacService rbacService;

  @GetMapping("/roles")
  public ApiResponse<List<RoleResponse>> getRoles() {
    return ApiResponse.of(rbacService.getRoles());
  }

  @PostMapping("/roles")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
    return ApiResponse.of(rbacService.createRole(request));
  }

  @PutMapping("/roles/{roleId}/permissions")
  public ApiResponse<RoleResponse> replaceRolePermissions(
      @PathVariable Long roleId, @Valid @RequestBody RolePermissionsRequest request) {
    return ApiResponse.of(rbacService.replaceRolePermissions(roleId, request));
  }

  @DeleteMapping("/roles/{roleId}")
  public ApiResponse<Void> deleteRole(@PathVariable Long roleId) {
    rbacService.deleteRole(roleId);
    return ApiResponse.ok();
  }

  @GetMapping("/permissions")
  public ApiResponse<List<PermissionResponse>> getPermissions() {
    return ApiResponse.of(rbacService.getPermissions());
  }

  @PutMapping("/accounts/{accountId}/users/{userId}/roles")
  public ApiResponse<UserRolesResponse> replaceUserRoles(
      @PathVariable Long accountId,
      @PathVariable Long userId,
      @Valid @RequestBody UserRolesRequest request) {
    return ApiResponse.of(rbacService.replaceUserRoles(accountId, userId, request));
  }
}
