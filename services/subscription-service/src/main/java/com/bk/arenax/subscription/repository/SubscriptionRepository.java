package com.bk.arenax.subscription.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bk.arenax.subscription.domain.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

  Optional<Subscription> findByAccountId(UUID accountId);
}
