package com.bk.arenax.competition;

import com.bk.arenax.competition.domain.entity.OutboxEvent;
import com.bk.arenax.competition.repository.OutboxEventRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "arenax.messaging.relay.enabled=true",
        "arenax.messaging.relay.poll-interval-ms=100"
})
class OutboxEventRelayIntegrationTests {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void relayPublishesUnpublishedEventsToExchangeAndMarksThemPublished() throws InterruptedException {
        OutboxEvent event = outboxEventRepository.save(newOutboxEvent("competition.match-completed.v1"));
        outboxEventRepository.save(publishedEvent("competition.match-completed.v1"));

        awaitPublished(event.getId());

        ArgumentCaptor<String> routingKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate, times(1)).convertAndSend(org.mockito.ArgumentMatchers.eq("arenax.events"),
                routingKey.capture(), message.capture());

        assertThat(routingKey.getValue()).isEqualTo("competition.match-completed.v1");
        assertThat(message.getValue()).isEqualTo("{\"eventType\":\"competition.match-completed.v1\"}");
    }

    private OutboxEvent newOutboxEvent(String eventType) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(eventType);
        outboxEvent.setEventVersion(1);
        outboxEvent.setCorrelationId(UUID.randomUUID());
        outboxEvent.setProducer("competition-service");
        outboxEvent.setOccurredAt(java.time.Instant.now());
        outboxEvent.setPayload("{\"eventType\":\"" + eventType + "\"}");
        return outboxEventRepository.save(outboxEvent);
    }

    private OutboxEvent publishedEvent(String eventType) {
        OutboxEvent outboxEvent = newOutboxEvent(eventType);
        outboxEvent.setPublishedAt(java.time.Instant.now());
        return outboxEventRepository.save(outboxEvent);
    }

    private void awaitPublished(UUID eventId) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            OutboxEvent event = outboxEventRepository.findById(eventId).orElseThrow();
            if (event.getPublishedAt() != null) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Outbox event was not published within timeout: " + eventId);
    }
}