package com.bk.arenax.adapter.repository;

import com.bk.arenax.domain.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface SubscriptionRepository
    extends JpaRepository<Subscription, Long>, QuerydslPredicateExecutor<Subscription> {}
