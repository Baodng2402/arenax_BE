package com.bk.arenax.tenant.repository;

import com.bk.arenax.tenant.domain.entity.Membership;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    boolean existsByAccountIdAndUserId(UUID accountId, UUID userId);

    Optional<Membership> findByAccountIdAndUserId(UUID accountId, UUID userId);

    List<Membership> findAllByUserIdOrderByCreatedAtAsc(UUID userId);

    List<Membership> findAllByAccountIdOrderByCreatedAtAsc(UUID accountId);
}
