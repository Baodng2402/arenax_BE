package com.bk.arenax.identity.service.support;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.domain.UserIdentifier;
import com.bk.arenax.identity.messaging.UserPasswordResetRequestedPayload;
import com.bk.arenax.identity.messaging.UserRegisteredPayload;
import com.bk.arenax.identity.messaging.UserVerificationRequestedPayload;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.messaging.EventEnvelope;

@Component
@RequiredArgsConstructor
public class IdentityEventPublisher {

  private final OutboxEventRepository outboxEventRepository;
  private final IdentityEventSerializer eventSerializer;

  public void publishVerificationRequested(
      User user,
      UserIdentifier identifier,
      String rawVerificationToken,
      Instant expiresAt,
      Instant occurredAt) {
    outboxEventRepository.save(
        OutboxEvent.create(
            "identity.user.verification-requested.v1",
            1,
            user.getId(),
            "identity-service",
            occurredAt,
            eventSerializer.writePayload(
                new EventEnvelope<>(
                    UUID.randomUUID(),
                    "identity.user.verification-requested.v1",
                    1,
                    occurredAt,
                    user.getId(),
                    "identity-service",
                    new UserVerificationRequestedPayload(
                        user.getId(),
                        identifier.getNormalizedValue(),
                        user.getFullName(),
                        rawVerificationToken,
                        expiresAt)))));
  }

  public void publishUserRegistered(User user, Instant occurredAt) {
    outboxEventRepository.save(
        OutboxEvent.create(
            "identity.user.registered.v2",
            2,
            user.getId(),
            "identity-service",
            occurredAt,
            eventSerializer.writePayload(
                new EventEnvelope<>(
                    UUID.randomUUID(),
                    "identity.user.registered.v2",
                    2,
                    occurredAt,
                    user.getId(),
                    "identity-service",
                    new UserRegisteredPayload(user.getId(), user.getFullName())))));
  }

  public void publishPasswordResetRequested(
      User user,
      UserIdentifier identifier,
      String rawResetToken,
      Instant expiresAt,
      Instant occurredAt) {
    outboxEventRepository.save(
        OutboxEvent.create(
            "identity.user.password-reset-requested.v1",
            1,
            user.getId(),
            "identity-service",
            occurredAt,
            eventSerializer.writePayload(
                new EventEnvelope<>(
                    UUID.randomUUID(),
                    "identity.user.password-reset-requested.v1",
                    1,
                    occurredAt,
                    user.getId(),
                    "identity-service",
                    new UserPasswordResetRequestedPayload(
                        user.getId(),
                        identifier.getNormalizedValue(),
                        user.getFullName(),
                        rawResetToken,
                        expiresAt)))));
  }
}
