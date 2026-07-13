package com.bk.arenax.tenant.repository;

import com.bk.arenax.tenant.domain.entity.Membership;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    boolean existsByAccountIdAndUserId(UUID accountId, UUID userId);
}
