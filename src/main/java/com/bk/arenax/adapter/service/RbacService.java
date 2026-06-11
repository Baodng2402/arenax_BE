package com.bk.arenax.adapter.service;

import com.bk.arenax.adapter.repository.AccountRepository;
import com.bk.arenax.adapter.repository.rbac.PermissionRepository;
import com.bk.arenax.adapter.repository.rbac.RoleRepository;
import com.bk.arenax.adapter.repository.UserRepository;
import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.rbac.Permission;
import com.bk.arenax.domain.rbac.Role;
import com.bk.arenax.domain.user.User;
import com.bk.arenax.dto.request.CreateRoleRequest;
import com.bk.arenax.dto.request.RolePermissionsRequest;
import com.bk.arenax.dto.request.UserRolesRequest;
import com.bk.arenax.dto.response.PermissionResponse;
import com.bk.arenax.dto.response.RoleResponse;
import com.bk.arenax.dto.response.UserRolesResponse;
import com.bk.arenax.infrastructure.exception.BadRequestException;
import com.bk.arenax.infrastructure.exception.ErrorCode;
import com.bk.arenax.infrastructure.exception.ResourceNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RbacService {
  private static final Set<String> PROTECTED_ROLES = Set.of("ADMIN", "USER");

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final RbacAuthorizationService authorizationService;

  @Transactional(readOnly = true)
  public List<RoleResponse> getRoles() {
    authorizationService.requirePermission("RBAC", "VIEW");
    return roleRepository.findAll().stream().map(RoleResponse::from).toList();
  }

  @Transactional(readOnly = true)
  public List<PermissionResponse> getPermissions() {
    authorizationService.requirePermission("RBAC", "VIEW");
    return permissionRepository.findAll().stream().map(PermissionResponse::from).toList();
  }

  @Transactional
  public RoleResponse createRole(CreateRoleRequest request) {
    authorizationService.requirePermission("RBAC", "MANAGE");
    String codeName = Role.normalizeCodeName(request.codeName());
    if (roleRepository.existsByCodeName(codeName)) {
      throw new BadRequestException(ErrorCode.BAD_REQUEST, "Role already exists: " + codeName);
    }
    Role role = Role.of(request.name(), codeName);
    role.setDescription(request.description());
    return RoleResponse.from(roleRepository.save(role));
  }

  @Transactional
  public RoleResponse replaceRolePermissions(Long roleId, RolePermissionsRequest request) {
    authorizationService.requirePermission("RBAC", "MANAGE");
    Role role = getRole(roleId);
    Set<String> codeNames = normalizePermissionCodes(request.permissionCodeNames());
    List<Permission> permissions = permissionRepository.findByCodeNameIn(codeNames);
    if (permissions.size() != codeNames.size()) {
      throw new BadRequestException(ErrorCode.BAD_REQUEST, "Some permissions do not exist");
    }
    role.replacePermissions(new HashSet<>(permissions));
    return RoleResponse.from(roleRepository.save(role));
  }

  @Transactional
  public UserRolesResponse replaceUserRoles(Long accountId, Long userId, UserRolesRequest request) {
    authorizationService.requirePermission(accountId, "RBAC", "MANAGE");
    Account account =
        accountRepository
            .findById(accountId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND, "Account not found: " + accountId));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        ErrorCode.USER_NOT_FOUND, "User not found: " + userId));
    Set<String> codeNames = normalizeRoleCodes(request.roleCodeNames());
    if (codeNames.isEmpty()) {
      throw new BadRequestException(ErrorCode.BAD_REQUEST, "User must have at least one role");
    }
    List<Role> roles = roleRepository.findByCodeNameIn(codeNames);
    if (roles.size() != codeNames.size()) {
      throw new BadRequestException(ErrorCode.BAD_REQUEST, "Some roles do not exist");
    }
    user.replaceRolesForAccount(account, new HashSet<>(roles));
    return UserRolesResponse.from(userRepository.save(user), account);
  }

  @Transactional
  public void deleteRole(Long roleId) {
    authorizationService.requirePermission("RBAC", "MANAGE");
    Role role = getRole(roleId);
    if (role.isSystemRole() || PROTECTED_ROLES.contains(role.getCodeName())) {
      throw new BadRequestException(ErrorCode.BAD_REQUEST, "System role cannot be deleted");
    }
    roleRepository.delete(role);
  }

  private Role getRole(Long roleId) {
    return roleRepository
        .findById(roleId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    ErrorCode.ENTITY_NOT_FOUND, "Role not found: " + roleId));
  }

  private Set<String> normalizePermissionCodes(List<String> values) {
    if (values == null) {
      return Set.of();
    }
    return values.stream()
        .map(Permission::normalizeCode)
        .collect(java.util.stream.Collectors.toSet());
  }

  private Set<String> normalizeRoleCodes(List<String> values) {
    if (values == null) {
      return Set.of();
    }
    return values.stream()
        .map(Role::normalizeCodeName)
        .collect(java.util.stream.Collectors.toSet());
  }
}
