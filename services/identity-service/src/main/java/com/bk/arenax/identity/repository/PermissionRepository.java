package com.bk.arenax.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bk.arenax.identity.domain.Permission;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
  Optional<Permission> findByCode(String code);
}
