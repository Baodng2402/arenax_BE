package com.bk.arenax.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import com.bk.arenax.tenant.domain.entity.Account;
import com.bk.arenax.tenant.domain.entity.Membership;
import com.bk.arenax.tenant.domain.entity.OutboxEvent;
import com.bk.arenax.tenant.domain.enums.AccountType;
import com.bk.arenax.tenant.domain.enums.MembershipRole;
import com.bk.arenax.tenant.messaging.EventEnvelope;
import com.bk.arenax.tenant.messaging.UserRegisteredPayload;
import com.bk.arenax.tenant.repository.AccountRepository;
import com.bk.arenax.tenant.repository.MembershipRepository;
import com.bk.arenax.tenant.repository.OutboxEventRepository;
import com.bk.arenax.tenant.service.UserRegistrationHandler;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserRegistrationHandlerIntegrationTests {

    @Autowired
    private UserRegistrationHandler userRegistrationHandler;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        membershipRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void handleCreatesPersonalAccountAndOwnerMembership() {
        UUID userId = UUID.fromString("7c109ef8-15d4-4d8a-a66a-c1f4138fb5ec");

        userRegistrationHandler.handle(userRegisteredEvent(userId, "Player One"));

        List<Account> accounts = accountRepository.findAll();
        List<Membership> memberships = membershipRepository.findAll();
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getOwnerUserId()).isEqualTo(userId);
        assertThat(accounts.getFirst().getName()).isEqualTo("Player One");
        assertThat(accounts.getFirst().getType()).isEqualTo(AccountType.PERSONAL);

        assertThat(memberships).hasSize(1);
        assertThat(memberships.getFirst().getUserId()).isEqualTo(userId);
        assertThat(memberships.getFirst().getAccountId()).isEqualTo(accounts.getFirst().getId());
        assertThat(memberships.getFirst().getRole()).isEqualTo(MembershipRole.OWNER);

        assertThat(outboxEvents).hasSize(1);
        assertThat(outboxEvents.getFirst().getEventType()).isEqualTo("tenant.personal-account-created.v1");
        assertThat(outboxEvents.getFirst().getCorrelationId())
                .isEqualTo(UUID.fromString("4608f3d6-bf56-497b-8abf-0de1a468fdcf"));
    }

    @Test
    void handleIsIdempotentForDuplicateEventDelivery() {
        UUID userId = UUID.fromString("a0941ec3-14d2-43d8-8179-9f95ca5a5f91");
        EventEnvelope<UserRegisteredPayload> event = userRegisteredEvent(userId, "Player Two");

        userRegistrationHandler.handle(event);
        userRegistrationHandler.handle(event);

        assertThat(accountRepository.findAll()).hasSize(1);
        assertThat(membershipRepository.findAll()).hasSize(1);
        assertThat(outboxEventRepository.findAll()).hasSize(1);
    }

    private EventEnvelope<UserRegisteredPayload> userRegisteredEvent(UUID userId, String displayName) {
        return new EventEnvelope<>(
                UUID.fromString("db100d23-8ec3-4487-8b3e-4c2efde1c7f0"),
                "identity.user.registered.v2",
                2,
                Instant.parse("2026-07-13T10:00:00Z"),
                UUID.fromString("4608f3d6-bf56-497b-8abf-0de1a468fdcf"),
                "identity-service",
                new UserRegisteredPayload(userId, displayName));
    }
}
