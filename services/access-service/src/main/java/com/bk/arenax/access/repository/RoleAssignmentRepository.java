package com.bk.arenax.access.repository;

import com.bk.arenax.access.domain.entity.RoleAssignment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, UUID> {

    Optional<RoleAssignment> findByUserIdAndAccountIdAndRoleCode(UUID userId, UUID accountId, String roleCode);
}
