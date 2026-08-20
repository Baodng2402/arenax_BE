package com.bk.arenax.competition.infrastructure.messaging;

import com.bk.arenax.competition.domain.entity.OutboxEvent;
import com.bk.arenax.competition.repository.OutboxEventRepository;
import com.bk.arenax.messaging.OutboxEventStore;
import com.bk.arenax.messaging.PendingOutboxEvent;
import java.util.List;
import org.springframework.stereotype.Component;

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