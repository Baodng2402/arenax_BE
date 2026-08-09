package com.bk.arenax.identity.repository;

import com.bk.arenax.identity.domain.EmailVerificationToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    void deleteAllByUserIdentifierId(UUID userIdentifierId);
}
