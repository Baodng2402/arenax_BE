package com.bk.arenax.subscription.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bk.arenax.subscription.domain.entity.OutboxEvent;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

  List<OutboxEvent> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
