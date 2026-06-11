package com.bk.arenax.adapter.repository.rbac;

import com.bk.arenax.domain.rbac.UserRoleAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface UserRoleAssignmentRepository
    extends JpaRepository<UserRoleAssignment, Long>, QuerydslPredicateExecutor<UserRoleAssignment> {
  List<UserRoleAssignment> findByUserId(Long userId);
}
