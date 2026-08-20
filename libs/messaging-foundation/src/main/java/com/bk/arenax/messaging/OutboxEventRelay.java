package com.bk.arenax.messaging;

import java.time.Instant;
import java.util.List;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.scheduling.annotation.Scheduled;

public class OutboxEventRelay {

    private static final String EXCHANGE = "arenax.events";

    private final AmqpTemplate amqpTemplate;
    private final OutboxEventStore outboxEventStore;

    public OutboxEventRelay(AmqpTemplate amqpTemplate, OutboxEventStore outboxEventStore) {
        this.amqpTemplate = amqpTemplate;
        this.outboxEventStore = outboxEventStore;
    }

    @Scheduled(fixedDelayString = "${arenax.messaging.relay.poll-interval-ms:5000}")
    public void publishPendingEvents() {
        List<? extends PendingOutboxEvent> pending = outboxEventStore.findPending();
        Instant publishedAt = Instant.now();
        for (PendingOutboxEvent event : pending) {
            try {
                amqpTemplate.convertAndSend(EXCHANGE, event.getEventType(), event.getPayload());
                event.setPublishedAt(publishedAt);
                outboxEventStore.save(event);
            } catch (RuntimeException exception) {
                // keep unpublished for the next poll
            }
        }
    }
}