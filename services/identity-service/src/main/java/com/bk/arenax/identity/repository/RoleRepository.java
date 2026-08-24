package com.bk.arenax.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bk.arenax.identity.domain.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
  Optional<Role> findByCode(String code);
}
