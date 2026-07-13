package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.entity.OnboardingProgress;
import com.bk.arenax.identity.domain.entity.OutboxEvent;
import com.bk.arenax.identity.domain.entity.User;
import com.bk.arenax.identity.domain.enums.UserStatus;
import com.bk.arenax.identity.dto.request.RegisterRequest;
import com.bk.arenax.identity.dto.response.RegisteredUserResponse;
import com.bk.arenax.identity.exception.DuplicateEmailException;
import com.bk.arenax.identity.messaging.EventEnvelope;
import com.bk.arenax.identity.messaging.UserRegisteredPayload;
import com.bk.arenax.identity.repository.OnboardingProgressRepository;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final OnboardingProgressRepository onboardingProgressRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public RegistrationService(
            UserRepository userRepository,
            OutboxEventRepository outboxEventRepository,
            OnboardingProgressRepository onboardingProgressRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.onboardingProgressRepository = onboardingProgressRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RegisteredUserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateEmailException();
        }

        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        user.setStatus(UserStatus.PROVISIONING);
        userRepository.save(user);

        persistUserRegisteredEvent(user);

        return new RegisteredUserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getStatus());
    }

    private void persistUserRegisteredEvent(User user) {
        UUID correlationId = UUID.randomUUID();
        EventEnvelope<UserRegisteredPayload> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                "identity.user.registered.v1",
                1,
                Instant.now(),
                correlationId,
                "identity-service",
                new UserRegisteredPayload(user.getId(), user.getEmail(), user.getDisplayName()));

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(envelope.eventType());
        outboxEvent.setEventVersion(envelope.eventVersion());
        outboxEvent.setCorrelationId(envelope.correlationId());
        outboxEvent.setProducer(envelope.producer());
        outboxEvent.setOccurredAt(envelope.occurredAt());
        outboxEvent.setPayload(writePayload(envelope));
        outboxEventRepository.save(outboxEvent);

        OnboardingProgress progress = new OnboardingProgress();
        progress.setCorrelationId(correlationId);
        progress.setUserId(user.getId());
        progress.setAuthorizationReady(false);
        progress.setSubscriptionReady(false);
        onboardingProgressRepository.save(progress);
    }

    private String writePayload(EventEnvelope<UserRegisteredPayload> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize event payload", exception);
        }
    }
}
