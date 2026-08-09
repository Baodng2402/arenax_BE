package com.bk.arenax.identity;

import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import java.util.List;
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
        OutboxEvent first = outboxEventRepository.save(
                OutboxEvent.create("identity.user.registered.v2", 2, UUID.randomUUID(),
                        "identity-service", java.time.Instant.now(), "{\"eventType\":\"identity.user.registered.v2\"}"));
        OutboxEvent second = outboxEventRepository.save(
                OutboxEvent.create("identity.user.verification-requested.v1", 1, UUID.randomUUID(),
                        "identity-service", java.time.Instant.now(), "{\"eventType\":\"identity.user.verification-requested.v1\"}"));
        OutboxEvent alreadyPublished = outboxEventRepository.save(
                OutboxEvent.create("identity.user.password-reset-requested.v1", 1, UUID.randomUUID(),
                        "identity-service", java.time.Instant.now(), "{\"eventType\":\"identity.user.password-reset-requested.v1\"}"));
        alreadyPublished.setPublishedAt(java.time.Instant.now());
        outboxEventRepository.save(alreadyPublished);

        awaitPublished(first.getId());
        awaitPublished(second.getId());

        ArgumentCaptor<String> routingKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate, times(2)).convertAndSend(org.mockito.ArgumentMatchers.eq("arenax.events"),
                routingKey.capture(), message.capture());

        assertThat(routingKey.getAllValues()).containsExactlyInAnyOrder(
                "identity.user.registered.v2", "identity.user.verification-requested.v1");
        assertThat(message.getAllValues()).containsExactlyInAnyOrder(
                "{\"eventType\":\"identity.user.registered.v2\"}",
                "{\"eventType\":\"identity.user.verification-requested.v1\"}");
        assertThat(outboxEventRepository.findById(alreadyPublished.getId()).orElseThrow().getPublishedAt()).isNotNull();
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