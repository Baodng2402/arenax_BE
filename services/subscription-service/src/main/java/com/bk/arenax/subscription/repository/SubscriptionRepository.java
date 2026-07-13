package com.bk.arenax.subscription.repository;

import com.bk.arenax.subscription.domain.entity.Subscription;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByAccountId(UUID accountId);
}
