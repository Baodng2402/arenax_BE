package com.bk.arenax.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import com.bk.arenax.subscription.domain.entity.OutboxEvent;
import com.bk.arenax.subscription.domain.entity.Subscription;
import com.bk.arenax.subscription.domain.enums.SubscriptionPlan;
import com.bk.arenax.subscription.domain.enums.SubscriptionStatus;
import com.bk.arenax.subscription.messaging.EventEnvelope;
import com.bk.arenax.subscription.messaging.PersonalAccountCreatedPayload;
import com.bk.arenax.subscription.repository.OutboxEventRepository;
import com.bk.arenax.subscription.repository.SubscriptionRepository;
import com.bk.arenax.subscription.service.PersonalAccountCreatedHandler;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PersonalAccountCreatedHandlerIntegrationTests {

    @Autowired
    private PersonalAccountCreatedHandler handler;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        subscriptionRepository.deleteAll();
    }

    @Test
    void handleCreatesFreeSubscriptionAndPublishesActivationEvent() {
        UUID accountId = UUID.fromString("55555555-5555-5555-5555-555555555555");

        handler.handle(personalAccountCreatedEvent(accountId, "Player One"));

        Subscription subscription = subscriptionRepository.findAll().getFirst();
        assertThat(subscription.getAccountId()).isEqualTo(accountId);
        assertThat(subscription.getPlan()).isEqualTo(SubscriptionPlan.FREE);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents)
                .extracting(OutboxEvent::getEventType)
                .containsExactly("subscription.activated.v1");
    }

    @Test
    void handleIsIdempotentForDuplicateEventDelivery() {
        EventEnvelope<PersonalAccountCreatedPayload> event =
                personalAccountCreatedEvent(UUID.fromString("66666666-6666-6666-6666-666666666666"), "Player Two");

        handler.handle(event);
        handler.handle(event);

        assertThat(subscriptionRepository.count()).isEqualTo(1);
        assertThat(outboxEventRepository.count()).isEqualTo(1);
    }

    private EventEnvelope<PersonalAccountCreatedPayload> personalAccountCreatedEvent(UUID accountId, String accountName) {
        return new EventEnvelope<>(
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                "tenant.personal-account-created.v1",
                1,
                Instant.parse("2026-07-13T12:00:00Z"),
                UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                "tenant-service",
                new PersonalAccountCreatedPayload(
                        UUID.fromString("77777777-7777-7777-7777-777777777777"),
                        accountId,
                        accountName));
    }
}
