package com.bk.arenax.adapter.repository.rbac;

import com.bk.arenax.domain.rbac.Permission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface PermissionRepository
    extends JpaRepository<Permission, Long>, QuerydslPredicateExecutor<Permission> {
  Optional<Permission> findByCodeName(String codeName);

  List<Permission> findByCodeNameIn(Collection<String> codeNames);

  boolean existsByCodeName(String codeName);
}
