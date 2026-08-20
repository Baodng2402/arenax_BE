package com.bk.arenax.tenant.service;

import com.bk.arenax.tenant.domain.entity.Account;
import com.bk.arenax.tenant.domain.entity.Membership;
import com.bk.arenax.tenant.domain.entity.OutboxEvent;
import com.bk.arenax.tenant.domain.enums.AccountStatus;
import com.bk.arenax.tenant.domain.enums.AccountType;
import com.bk.arenax.tenant.domain.enums.MembershipRole;
import com.bk.arenax.messaging.EventEnvelope;
import com.bk.arenax.tenant.messaging.PersonalAccountCreatedPayload;
import com.bk.arenax.tenant.messaging.UserRegisteredPayload;
import com.bk.arenax.tenant.repository.AccountRepository;
import com.bk.arenax.tenant.repository.MembershipRepository;
import com.bk.arenax.tenant.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegistrationHandler {

    private final AccountRepository accountRepository;
    private final MembershipRepository membershipRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public UserRegistrationHandler(
            AccountRepository accountRepository,
            MembershipRepository membershipRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.membershipRepository = membershipRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void handle(EventEnvelope<UserRegisteredPayload> event) {
        UserRegisteredPayload payload = event.payload();

        Account account = accountRepository.findByOwnerUserIdAndType(payload.userId(), AccountType.PERSONAL)
                .orElseGet(() -> createPersonalAccount(payload));

        if (!membershipRepository.existsByAccountIdAndUserId(account.getId(), payload.userId())) {
            Membership membership = new Membership();
            membership.setAccountId(account.getId());
            membership.setUserId(payload.userId());
            membership.setRole(MembershipRole.OWNER);
            membershipRepository.save(membership);
        }

        persistOutboxEvent(event, account);
    }

    private Account createPersonalAccount(UserRegisteredPayload payload) {
        Account account = new Account();
        account.setOwnerUserId(payload.userId());
        account.setName(payload.displayName().trim());
        account.setType(AccountType.PERSONAL);
        account.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    private void persistOutboxEvent(EventEnvelope<UserRegisteredPayload> sourceEvent, Account account) {
        if (outboxEventRepository.findByEventTypeAndCorrelationId(
                        "tenant.personal-account-created.v1", sourceEvent.correlationId())
                .isPresent()) {
            return;
        }

        EventEnvelope<PersonalAccountCreatedPayload> envelope = new EventEnvelope<>(
                java.util.UUID.randomUUID(),
                "tenant.personal-account-created.v1",
                1,
                Instant.now(),
                sourceEvent.correlationId(),
                "tenant-service",
                new PersonalAccountCreatedPayload(account.getOwnerUserId(), account.getId(), account.getName()));

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(envelope.eventType());
        outboxEvent.setEventVersion(envelope.eventVersion());
        outboxEvent.setCorrelationId(envelope.correlationId());
        outboxEvent.setProducer(envelope.producer());
        outboxEvent.setOccurredAt(envelope.occurredAt());
        outboxEvent.setPayload(writePayload(envelope));
        outboxEventRepository.save(outboxEvent);
    }

    private String writePayload(EventEnvelope<PersonalAccountCreatedPayload> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize tenant event payload", exception);
        }
    }
}
