package com.bk.arenax.access.service;

import com.bk.arenax.access.domain.entity.OutboxEvent;
import com.bk.arenax.access.domain.entity.Permission;
import com.bk.arenax.access.domain.entity.Role;
import com.bk.arenax.access.domain.entity.RoleAssignment;
import com.bk.arenax.access.messaging.AuthorizationChangedPayload;
import com.bk.arenax.access.messaging.DefaultRoleGrantedPayload;
import com.bk.arenax.access.messaging.EventEnvelope;
import com.bk.arenax.access.messaging.PersonalAccountCreatedPayload;
import com.bk.arenax.access.repository.OutboxEventRepository;
import com.bk.arenax.access.repository.PermissionRepository;
import com.bk.arenax.access.repository.RoleAssignmentRepository;
import com.bk.arenax.access.repository.RoleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonalAccountCreatedHandler {

    private static final String USER_ROLE_CODE = "USER";
    private static final Set<String> DEFAULT_PERMISSION_CODES = Set.of("MATCH:CREATE", "MATCH:JOIN", "RANKING:READ");

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public PersonalAccountCreatedHandler(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            RoleAssignmentRepository roleAssignmentRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void handle(EventEnvelope<PersonalAccountCreatedPayload> event) {
        PersonalAccountCreatedPayload payload = event.payload();
        if (roleAssignmentRepository.findByUserIdAndAccountIdAndRoleCode(payload.userId(), payload.accountId(), USER_ROLE_CODE)
                .isPresent()) {
            return;
        }

        Role role = ensureUserRole();

        RoleAssignment assignment = new RoleAssignment();
        assignment.setUserId(payload.userId());
        assignment.setAccountId(payload.accountId());
        assignment.setRoleCode(USER_ROLE_CODE);
        roleAssignmentRepository.save(assignment);

        persistOutboxEvent(
                "access.default-role-granted.v1",
                event,
                new DefaultRoleGrantedPayload(payload.userId(), payload.accountId(), USER_ROLE_CODE));
        persistOutboxEvent(
                "access.authorization-changed.v1",
                event,
                new AuthorizationChangedPayload(
                        payload.userId(),
                        payload.accountId(),
                        Set.of(USER_ROLE_CODE),
                        role.getPermissions().stream().map(Permission::getCode).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))));
    }

    private Role ensureUserRole() {
        return roleRepository.findByCode(USER_ROLE_CODE).orElseGet(() -> {
            Role role = new Role();
            role.setCode(USER_ROLE_CODE);
            role.setName("Default User");
            role.setPermissions(resolveDefaultPermissions());
            return roleRepository.save(role);
        });
    }

    private Set<Permission> resolveDefaultPermissions() {
        LinkedHashSet<Permission> permissions = new LinkedHashSet<>();
        for (String code : DEFAULT_PERMISSION_CODES) {
            Permission permission = permissionRepository.findByCode(code).orElseGet(() -> {
                Permission created = new Permission();
                created.setCode(code);
                created.setName(code);
                return permissionRepository.save(created);
            });
            permissions.add(permission);
        }
        return permissions;
    }

    private void persistOutboxEvent(String eventType, EventEnvelope<PersonalAccountCreatedPayload> sourceEvent, Object payload) {
        if (outboxEventRepository.findAll().stream().anyMatch(existing -> existing.getEventType().equals(eventType)
                && existing.getCorrelationId().equals(sourceEvent.correlationId()))) {
            return;
        }

        EventEnvelope<Object> envelope = new EventEnvelope<>(
                java.util.UUID.randomUUID(),
                eventType,
                1,
                Instant.now(),
                sourceEvent.correlationId(),
                "access-service",
                payload);

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(envelope.eventType());
        outboxEvent.setEventVersion(envelope.eventVersion());
        outboxEvent.setCorrelationId(envelope.correlationId());
        outboxEvent.setProducer(envelope.producer());
        outboxEvent.setOccurredAt(envelope.occurredAt());
        outboxEvent.setPayload(writePayload(envelope));
        outboxEventRepository.save(outboxEvent);
    }

    private String writePayload(EventEnvelope<Object> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize access event payload", exception);
        }
    }
}
