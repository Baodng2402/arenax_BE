package com.bk.arenax.competition.infrastructure.messaging;

import com.bk.arenax.competition.domain.entity.OutboxEvent;
import com.bk.arenax.competition.repository.OutboxEventRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "arenax.messaging.relay.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxEventRelay {

    private static final String EXCHANGE = "arenax.events";

    private final RabbitTemplate rabbitTemplate;
    private final OutboxEventRepository outboxEventRepository;

    public OutboxEventRelay(RabbitTemplate rabbitTemplate, OutboxEventRepository outboxEventRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Scheduled(fixedDelayString = "${arenax.messaging.relay.poll-interval-ms:5000}")
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
        Instant publishedAt = Instant.now();
        for (OutboxEvent event : pending) {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, event.getEventType(), event.getPayload());
                event.setPublishedAt(publishedAt);
                outboxEventRepository.save(event);
            } catch (RuntimeException exception) {
                // keep unpublished for the next poll
            }
        }
    }
}