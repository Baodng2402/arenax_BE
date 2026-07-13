package com.bk.arenax.tenant.repository;

import com.bk.arenax.tenant.domain.entity.Account;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByOwnerUserIdAndType(UUID ownerUserId, com.bk.arenax.tenant.domain.enums.AccountType type);
}
