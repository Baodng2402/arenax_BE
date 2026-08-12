package com.bk.arenax.tenant;

import com.bk.arenax.tenant.domain.entity.Account;
import com.bk.arenax.tenant.domain.entity.Membership;
import com.bk.arenax.tenant.domain.enums.AccountType;
import com.bk.arenax.tenant.domain.enums.MembershipRole;
import com.bk.arenax.tenant.infrastructure.messaging.UserRegisteredEventConsumer;
import com.bk.arenax.tenant.repository.AccountRepository;
import com.bk.arenax.tenant.repository.MembershipRepository;
import com.bk.arenax.tenant.repository.OutboxEventRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRegisteredEventConsumerIntegrationTests {

    @Autowired
    private UserRegisteredEventConsumer consumer;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void consumesRegisteredEventCreatesPersonalAccountAndOwnerMembership() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        String envelope = registeredEnvelope(userId, correlationId, "Kane");

        consumer.onUserRegistered(envelope);

        Account account = accountRepository.findByOwnerUserIdAndType(userId, AccountType.PERSONAL).orElseThrow();
        assertThat(account.getName()).isEqualTo("Kane");
        assertThat(membershipRepository.findByAccountIdAndUserId(account.getId(), userId))
                .get()
                .extracting(Membership::getRole)
                .isEqualTo(MembershipRole.OWNER);
        assertThat(outboxEventRepository.findByEventTypeAndCorrelationId(
                "tenant.personal-account-created.v1", correlationId)).isPresent();
    }

    @Test
    void consumingTheSameEventTwiceIsIdempotent() throws Exception {
        UUID userId = UUID.randomUUID();
        String envelope = registeredEnvelope(userId, UUID.randomUUID(), "Aya");

        consumer.onUserRegistered(envelope);
        consumer.onUserRegistered(envelope);

        assertThat(accountRepository.findAllByOwnerUserIdOrderByCreatedAtAsc(userId)).hasSize(1);
        assertThat(membershipRepository.findAllByUserIdOrderByCreatedAtAsc(userId)).hasSize(1);
    }

    private String registeredEnvelope(UUID userId, UUID correlationId, String displayName) {
        return """
                {
                  "eventId": "%s",
                  "eventType": "identity.user.registered.v2",
                  "eventVersion": 2,
                  "occurredAt": "2026-08-10T00:00:00Z",
                  "correlationId": "%s",
                  "producer": "identity-service",
                  "payload": {"userId": "%s", "displayName": "%s"}
                }
                """.formatted(UUID.randomUUID(), correlationId, userId, displayName);
    }
}