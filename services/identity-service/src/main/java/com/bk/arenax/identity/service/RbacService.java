package com.bk.arenax.identity.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bk.arenax.identity.domain.Permission;
import com.bk.arenax.identity.domain.RoleAssignment;
import com.bk.arenax.identity.repository.RoleAssignmentRepository;
import com.bk.arenax.identity.repository.RoleRepository;

@Service
public class RbacService {
  private final RoleAssignmentRepository roleAssignmentRepository;
  private final RoleRepository roleRepository;

  public RbacService(
      RoleAssignmentRepository roleAssignmentRepository, RoleRepository roleRepository) {
    this.roleAssignmentRepository = roleAssignmentRepository;
    this.roleRepository = roleRepository;
  }

  @Transactional(readOnly = true)
  public RbacDetails getUserRbac(UUID userId, UUID accountId) {
    List<RoleAssignment> assignments;
    if (accountId == null) {
      assignments = List.of();
    } else {
      assignments = roleAssignmentRepository.findByUserIdAndAccountId(userId, accountId);
    }

    List<String> roleCodes =
        assignments.stream()
            .map(RoleAssignment::getRoleCode)
            .distinct()
            .collect(Collectors.toList());

    List<String> permissions =
        roleCodes.stream()
            .flatMap(code -> roleRepository.findByCode(code).stream())
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getCode)
            .distinct()
            .collect(Collectors.toList());

    return new RbacDetails(roleCodes, permissions);
  }

  public record RbacDetails(List<String> roles, List<String> permissions) {}
}
