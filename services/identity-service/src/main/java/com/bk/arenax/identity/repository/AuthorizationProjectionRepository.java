package com.bk.arenax.identity.repository;

import com.bk.arenax.identity.domain.entity.AuthorizationProjection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizationProjectionRepository extends JpaRepository<AuthorizationProjection, UUID> {

    Optional<AuthorizationProjection> findByUserIdAndAccountId(UUID userId, UUID accountId);

    Optional<AuthorizationProjection> findFirstByUserIdOrderByCreatedAtAsc(UUID userId);
}
