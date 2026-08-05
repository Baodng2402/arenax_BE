package com.bk.arenax.identity.repository;

import com.bk.arenax.identity.domain.RoleAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, UUID> {
    List<RoleAssignment> findByUserId(UUID userId);
}
