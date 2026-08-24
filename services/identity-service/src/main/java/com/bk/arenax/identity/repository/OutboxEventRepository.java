package com.bk.arenax.identity.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bk.arenax.identity.domain.OutboxEvent;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

  List<OutboxEvent> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
