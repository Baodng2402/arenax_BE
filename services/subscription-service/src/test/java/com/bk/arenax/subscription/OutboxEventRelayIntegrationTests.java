package com.bk.arenax.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.bk.arenax.subscription.domain.entity.OutboxEvent;
import com.bk.arenax.subscription.repository.OutboxEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
        OutboxEvent event = outboxEventRepository.save(newOutboxEvent("subscription.activated.v1"));
        outboxEventRepository.save(publishedEvent("subscription.changed.v1"));

        awaitPublished(event.getId());

        ArgumentCaptor<String> routingKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq("arenax.events"),
                routingKey.capture(), message.capture());

        assertThat(routingKey.getValue()).isEqualTo("subscription.activated.v1");
        assertThat(message.getValue()).isEqualTo("{\"eventType\":\"subscription.activated.v1\"}");
    }

    private OutboxEvent newOutboxEvent(String eventType) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(eventType);
        outboxEvent.setEventVersion(1);
        outboxEvent.setCorrelationId(UUID.randomUUID());
        outboxEvent.setProducer("subscription-service");
        outboxEvent.setOccurredAt(Instant.now());
        outboxEvent.setPayload("{\"eventType\":\"" + eventType + "\"}");
        return outboxEventRepository.save(outboxEvent);
    }

    private OutboxEvent publishedEvent(String eventType) {
        OutboxEvent outboxEvent = newOutboxEvent(eventType);
        outboxEvent.setPublishedAt(Instant.now());
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
