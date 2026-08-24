package com.bk.arenax.identity.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bk.arenax.identity.domain.RoleAssignment;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, UUID> {
  List<RoleAssignment> findByUserId(UUID userId);

  List<RoleAssignment> findByUserIdAndAccountId(UUID userId, UUID accountId);
}
