package com.bk.arenax.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bk.arenax.subscription.domain.enums.SubscriptionPlan;
import com.bk.arenax.subscription.domain.enums.SubscriptionStatus;
import com.bk.arenax.subscription.infrastructure.messaging.PersonalAccountCreatedEventConsumer;
import com.bk.arenax.subscription.repository.OutboxEventRepository;
import com.bk.arenax.subscription.repository.SubscriptionRepository;

@SpringBootTest
class PersonalAccountCreatedEventConsumerIntegrationTests {

  @Autowired private PersonalAccountCreatedEventConsumer consumer;

  @Autowired private SubscriptionRepository subscriptionRepository;

  @Autowired private OutboxEventRepository outboxEventRepository;

  @Test
  void consumesPersonalAccountCreatedEventAndActivatesFreeSubscription() throws Exception {
    UUID accountId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    String envelope =
        """
                {
                  "eventId": "%s",
                  "eventType": "tenant.personal-account-created.v1",
                  "eventVersion": 1,
                  "occurredAt": "2026-08-10T00:00:00Z",
                  "correlationId": "%s",
                  "producer": "tenant-service",
                  "payload": {"userId": "%s", "accountId": "%s", "accountName": "Kane"}
                }
                """
            .formatted(UUID.randomUUID(), correlationId, UUID.randomUUID(), accountId);

    consumer.onPersonalAccountCreated(envelope);

    var subscription = subscriptionRepository.findByAccountId(accountId).orElseThrow();
    assertThat(subscription.getPlan()).isEqualTo(SubscriptionPlan.FREE);
    assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    assertThat(outboxEventRepository.findAll())
        .anyMatch(
            event ->
                event.getEventType().equals("subscription.activated.v1")
                    && event.getCorrelationId().equals(correlationId));
  }

  @Test
  void consumingTheSameEventTwiceKeepsOnlyOneSubscription() throws Exception {
    UUID accountId = UUID.randomUUID();
    String envelope =
        """
                {
                  "eventId": "%s",
                  "eventType": "tenant.personal-account-created.v1",
                  "eventVersion": 1,
                  "occurredAt": "2026-08-10T00:00:00Z",
                  "correlationId": "%s",
                  "producer": "tenant-service",
                  "payload": {"userId": "%s", "accountId": "%s", "accountName": "Aya"}
                }
                """
            .formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), accountId);

    consumer.onPersonalAccountCreated(envelope);
    consumer.onPersonalAccountCreated(envelope);

    assertThat(subscriptionRepository.findByAccountId(accountId)).isPresent();
    assertThat(subscriptionRepository.findAll())
        .filteredOn(subscription -> subscription.getAccountId().equals(accountId))
        .hasSize(1);
  }
}
