package com.bk.arenax.tenant.repository;

import com.bk.arenax.tenant.domain.entity.Account;
import com.bk.arenax.tenant.domain.enums.AccountType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByOwnerUserIdAndType(UUID ownerUserId, AccountType type);

    List<Account> findAllByOwnerUserIdOrderByCreatedAtAsc(UUID ownerUserId);
}
