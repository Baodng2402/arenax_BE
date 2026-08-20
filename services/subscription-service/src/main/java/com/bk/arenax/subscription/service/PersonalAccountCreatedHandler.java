package com.bk.arenax.subscription.service;

import com.bk.arenax.subscription.domain.entity.OutboxEvent;
import com.bk.arenax.subscription.domain.entity.Subscription;
import com.bk.arenax.subscription.domain.enums.SubscriptionPlan;
import com.bk.arenax.subscription.domain.enums.SubscriptionStatus;
import com.bk.arenax.messaging.EventEnvelope;
import com.bk.arenax.subscription.messaging.PersonalAccountCreatedPayload;
import com.bk.arenax.subscription.messaging.SubscriptionActivatedPayload;
import com.bk.arenax.subscription.repository.OutboxEventRepository;
import com.bk.arenax.subscription.repository.SubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonalAccountCreatedHandler {

    private final SubscriptionRepository subscriptionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public PersonalAccountCreatedHandler(
            SubscriptionRepository subscriptionRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void handle(EventEnvelope<PersonalAccountCreatedPayload> event) {
        PersonalAccountCreatedPayload payload = event.payload();
        if (subscriptionRepository.findByAccountId(payload.accountId()).isPresent()) {
            return;
        }

        Subscription subscription = new Subscription();
        subscription.setAccountId(payload.accountId());
        subscription.setPlan(SubscriptionPlan.FREE);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);

        persistOutboxEvent(event, subscription);
    }

    private void persistOutboxEvent(EventEnvelope<PersonalAccountCreatedPayload> sourceEvent, Subscription subscription) {
        EventEnvelope<SubscriptionActivatedPayload> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                "subscription.activated.v1",
                1,
                Instant.now(),
                sourceEvent.correlationId(),
                "subscription-service",
                new SubscriptionActivatedPayload(
                        subscription.getAccountId(),
                        subscription.getPlan().name(),
                        subscription.getStatus().name()));

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(envelope.eventType());
        outboxEvent.setEventVersion(envelope.eventVersion());
        outboxEvent.setCorrelationId(envelope.correlationId());
        outboxEvent.setProducer(envelope.producer());
        outboxEvent.setOccurredAt(envelope.occurredAt());
        outboxEvent.setPayload(writePayload(envelope));
        outboxEventRepository.save(outboxEvent);
    }

    private String writePayload(EventEnvelope<SubscriptionActivatedPayload> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize subscription event payload", exception);
        }
    }
}
