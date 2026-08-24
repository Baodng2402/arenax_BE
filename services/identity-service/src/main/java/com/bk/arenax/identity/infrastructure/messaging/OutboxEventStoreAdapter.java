package com.bk.arenax.identity.infrastructure.messaging;

import java.util.List;

import org.springframework.stereotype.Component;

import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.messaging.OutboxEventStore;
import com.bk.arenax.messaging.PendingOutboxEvent;

@Component
public class OutboxEventStoreAdapter implements OutboxEventStore {

  private final OutboxEventRepository outboxEventRepository;

  public OutboxEventStoreAdapter(OutboxEventRepository outboxEventRepository) {
    this.outboxEventRepository = outboxEventRepository;
  }

  @Override
  public List<? extends PendingOutboxEvent> findPending() {
    return outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
  }

  @Override
  public PendingOutboxEvent save(PendingOutboxEvent event) {
    return outboxEventRepository.save((OutboxEvent) event);
  }
}
