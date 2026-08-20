package com.bk.arenax.subscription.service;

import com.bk.arenax.subscription.controller.dto.CurrentSubscriptionResponse;
import com.bk.arenax.subscription.domain.entity.OutboxEvent;
import com.bk.arenax.subscription.domain.entity.Subscription;
import com.bk.arenax.subscription.domain.enums.SubscriptionPlan;
import com.bk.arenax.subscription.domain.enums.SubscriptionStatus;
import com.bk.arenax.messaging.EventEnvelope;
import com.bk.arenax.subscription.messaging.SubscriptionChangedPayload;
import com.bk.arenax.subscription.repository.OutboxEventRepository;
import com.bk.arenax.subscription.repository.SubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public CurrentSubscriptionResponse getCurrent(UUID accountId) {
        return toResponse(requireSubscription(requireAccountId(accountId)));
    }

    @Transactional
    public CurrentSubscriptionResponse changePlan(UUID accountId, String rawPlan) {
        Subscription subscription = requireSubscription(requireAccountId(accountId));
        SubscriptionPlan plan = parsePlan(rawPlan);
        if (subscription.getPlan() != plan || subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            subscription.setPlan(plan);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            persistOutboxEvent("subscription.changed.v1", subscription);
        }
        return toResponse(subscription);
    }

    @Transactional
    public CurrentSubscriptionResponse cancel(UUID accountId) {
        Subscription subscription = requireSubscription(requireAccountId(accountId));
        if (subscription.getStatus() != SubscriptionStatus.CANCELLED) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            persistOutboxEvent("subscription.cancelled.v1", subscription);
        }
        return toResponse(subscription);
    }

    private UUID requireAccountId(UUID accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account context is required");
        }
        return accountId;
    }

    private Subscription requireSubscription(UUID accountId) {
        return subscriptionRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found for account"));
    }

    private SubscriptionPlan parsePlan(String rawPlan) {
        if (rawPlan == null || rawPlan.isBlank()) {
            throw new IllegalArgumentException("Plan must not be blank");
        }
        try {
            return SubscriptionPlan.valueOf(rawPlan.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported subscription plan");
        }
    }

    private CurrentSubscriptionResponse toResponse(Subscription subscription) {
        return new CurrentSubscriptionResponse(
                subscription.getAccountId(),
                subscription.getPlan().name(),
                subscription.getStatus().name(),
                entitlements(subscription.getPlan()));
    }

    private List<String> entitlements(SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> List.of("ACCOUNT_CORE", "BASIC_RANKING");
            case PRO -> List.of("ACCOUNT_CORE", "BASIC_RANKING", "ADVANCED_RANKING", "PRIORITY_SUPPORT");
            case TEAM -> List.of("ACCOUNT_CORE", "BASIC_RANKING", "ADVANCED_RANKING", "TEAM_WORKSPACES", "MEMBER_MANAGEMENT");
        };
    }

    private void persistOutboxEvent(String eventType, Subscription subscription) {
        EventEnvelope<SubscriptionChangedPayload> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                1,
                Instant.now(),
                subscription.getAccountId(),
                "subscription-service",
                new SubscriptionChangedPayload(
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

    private String writePayload(EventEnvelope<SubscriptionChangedPayload> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize subscription event payload", exception);
        }
    }
}
