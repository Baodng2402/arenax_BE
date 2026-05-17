package com.bk.arenax.adapter.repository;

import com.bk.arenax.domain.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSubscriptionRepository extends JpaRepository<Subscription, Long> {
}
