package com.bk.arenax.access;

import static org.assertj.core.api.Assertions.assertThat;

import com.bk.arenax.access.domain.entity.OutboxEvent;
import com.bk.arenax.access.domain.entity.Permission;
import com.bk.arenax.access.domain.entity.Role;
import com.bk.arenax.access.domain.entity.RoleAssignment;
import com.bk.arenax.access.messaging.EventEnvelope;
import com.bk.arenax.access.messaging.PersonalAccountCreatedPayload;
import com.bk.arenax.access.repository.OutboxEventRepository;
import com.bk.arenax.access.repository.PermissionRepository;
import com.bk.arenax.access.repository.RoleAssignmentRepository;
import com.bk.arenax.access.repository.RoleRepository;
import com.bk.arenax.access.service.PersonalAccountCreatedHandler;
import java.time.Instant;
import java.util.List;
import java.util.Set;
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
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        roleAssignmentRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    @Test
    void handleGrantsDefaultUserRoleAndPublishesAccessEvents() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID accountId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        handler.handle(personalAccountCreatedEvent(userId, accountId, "Player One"));

        List<Permission> permissions = permissionRepository.findAll();
        assertThat(permissions)
                .extracting(Permission::getCode)
                .containsExactlyInAnyOrder("MATCH:CREATE", "MATCH:JOIN", "RANKING:READ");

        Role role = roleRepository.findByCode("USER").orElseThrow();
        assertThat(role.getPermissions())
                .extracting(Permission::getCode)
                .containsExactlyInAnyOrder("MATCH:CREATE", "MATCH:JOIN", "RANKING:READ");

        RoleAssignment assignment = roleAssignmentRepository.findAll().getFirst();
        assertThat(assignment.getUserId()).isEqualTo(userId);
        assertThat(assignment.getAccountId()).isEqualTo(accountId);
        assertThat(assignment.getRoleCode()).isEqualTo("USER");

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents)
                .extracting(OutboxEvent::getEventType)
                .containsExactlyInAnyOrder(
                        "access.default-role-granted.v1",
                        "access.authorization-changed.v1");
    }

    @Test
    void handleIsIdempotentForDuplicateEventDelivery() {
        EventEnvelope<PersonalAccountCreatedPayload> event = personalAccountCreatedEvent(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "Player Two");

        handler.handle(event);
        handler.handle(event);

        assertThat(permissionRepository.count()).isEqualTo(3);
        assertThat(roleRepository.count()).isEqualTo(1);
        assertThat(roleAssignmentRepository.count()).isEqualTo(1);
        assertThat(outboxEventRepository.findAll())
                .extracting(OutboxEvent::getEventType)
                .containsExactlyInAnyOrder(
                        "access.default-role-granted.v1",
                        "access.authorization-changed.v1");
    }

    private EventEnvelope<PersonalAccountCreatedPayload> personalAccountCreatedEvent(
            UUID userId, UUID accountId, String accountName) {
        return new EventEnvelope<>(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "tenant.personal-account-created.v1",
                1,
                Instant.parse("2026-07-13T10:15:30Z"),
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                "tenant-service",
                new PersonalAccountCreatedPayload(userId, accountId, accountName));
    }
}
