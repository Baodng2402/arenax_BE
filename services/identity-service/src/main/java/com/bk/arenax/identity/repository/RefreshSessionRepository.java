package com.bk.arenax.identity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bk.arenax.identity.domain.RefreshSession;

@Repository
public interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {
  Optional<RefreshSession> findByTokenHash(String tokenHash);

  List<RefreshSession> findAllByUserId(UUID userId);
}
