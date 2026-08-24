package com.bk.arenax.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bk.arenax.identity.domain.EmailVerificationToken;

@Repository
public interface EmailVerificationTokenRepository
    extends JpaRepository<EmailVerificationToken, UUID> {
  Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

  void deleteAllByUserIdentifierId(UUID userIdentifierId);
}
