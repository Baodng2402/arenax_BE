package com.bk.arenax.identity.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bk.arenax.identity.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByUsername(String username);
}
