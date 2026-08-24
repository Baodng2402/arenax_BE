package com.bk.arenax.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bk.arenax.identity.domain.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
  Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}
