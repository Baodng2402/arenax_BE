package com.bk.arenax.competition.repository;

import com.bk.arenax.competition.domain.entity.OutboxEvent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    Optional<OutboxEvent> findByEventTypeAndCorrelationId(String eventType, UUID correlationId);
}
