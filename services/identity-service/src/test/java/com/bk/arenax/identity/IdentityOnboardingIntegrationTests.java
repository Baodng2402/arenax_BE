package com.bk.arenax.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bk.arenax.identity.domain.entity.OutboxEvent;
import com.bk.arenax.identity.domain.entity.User;
import com.bk.arenax.identity.domain.enums.UserStatus;
import com.bk.arenax.identity.messaging.AuthorizationChangedPayload;
import com.bk.arenax.identity.messaging.DefaultRoleGrantedPayload;
import com.bk.arenax.identity.messaging.EventEnvelope;
import com.bk.arenax.identity.messaging.SubscriptionActivatedPayload;
import com.bk.arenax.identity.repository.AuthorizationProjectionRepository;
import com.bk.arenax.identity.repository.OnboardingProgressRepository;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.bk.arenax.identity.service.AuthorizationChangedHandler;
import com.bk.arenax.identity.service.DefaultRoleGrantedHandler;
import com.bk.arenax.identity.service.SubscriptionActivatedHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class IdentityOnboardingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OnboardingProgressRepository onboardingProgressRepository;

    @Autowired
    private AuthorizationProjectionRepository authorizationProjectionRepository;

    @Autowired
    private DefaultRoleGrantedHandler defaultRoleGrantedHandler;

    @Autowired
    private AuthorizationChangedHandler authorizationChangedHandler;

    @Autowired
    private SubscriptionActivatedHandler subscriptionActivatedHandler;

    @Autowired
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        authorizationProjectionRepository.deleteAll();
        onboardingProgressRepository.deleteAll();
        outboxEventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void defaultRoleGrantAloneDoesNotActivateUser() throws Exception {
        RegistrationContext registration = registerUser("player2@arenax.dev", "Player Two");
        UUID accountId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        defaultRoleGrantedHandler.handle(new EventEnvelope<>(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "access.default-role-granted.v1",
                1,
                Instant.parse("2026-07-13T10:00:00Z"),
                registration.correlationId(),
                "access-service",
                new DefaultRoleGrantedPayload(registration.user().getId(), accountId, "USER")));

        User user = userRepository.findById(registration.user().getId()).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.PROVISIONING);
    }

    @Test
    void onboardingCompletesAfterAuthorizationAndSubscriptionEvents() throws Exception {
        RegistrationContext registration = registerUser("player3@arenax.dev", "Player Three");
        UUID accountId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        defaultRoleGrantedHandler.handle(new EventEnvelope<>(
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                "access.default-role-granted.v1",
                1,
                Instant.parse("2026-07-13T10:00:00Z"),
                registration.correlationId(),
                "access-service",
                new DefaultRoleGrantedPayload(registration.user().getId(), accountId, "USER")));

        authorizationChangedHandler.handle(new EventEnvelope<>(
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                "access.authorization-changed.v1",
                1,
                Instant.parse("2026-07-13T10:00:05Z"),
                registration.correlationId(),
                "access-service",
                new AuthorizationChangedPayload(
                        registration.user().getId(),
                        accountId,
                        Set.of("USER"),
                        Set.of("MATCH:CREATE", "MATCH:JOIN", "RANKING:READ"))));

        subscriptionActivatedHandler.handle(new EventEnvelope<>(
                UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                "subscription.activated.v1",
                1,
                Instant.parse("2026-07-13T10:00:10Z"),
                registration.correlationId(),
                "subscription-service",
                new SubscriptionActivatedPayload(accountId, "FREE", "ACTIVE")));

        User activatedUser = userRepository.findById(registration.user().getId()).orElseThrow();
        assertThat(activatedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(activatedUser.getActiveAccountId()).isEqualTo(accountId);

        ObjectNode loginRequest = objectMapper.createObjectNode();
        loginRequest.put("email", registration.user().getEmail());
        loginRequest.put("password", "secret123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        Jwt jwt = jwtDecoder.decode(responseBody.get("accessToken").asText());

        assertThat(jwt.getClaimAsString("account_id")).isEqualTo(accountId.toString());
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("USER");
        assertThat(jwt.getClaimAsStringList("permissions"))
                .containsExactlyInAnyOrder("MATCH:CREATE", "MATCH:JOIN", "RANKING:READ");
    }

    private RegistrationContext registerUser(String email, String displayName) throws Exception {
        ObjectNode registerRequest = objectMapper.createObjectNode();
        registerRequest.put("email", email);
        registerRequest.put("password", "secret123");
        registerRequest.put("displayName", displayName);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(status().isCreated());

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        OutboxEvent outboxEvent = outboxEventRepository.findAll().stream()
                .filter(event -> "identity.user.registered.v1".equals(event.getEventType()))
                .findFirst()
                .orElseThrow();
        return new RegistrationContext(user, outboxEvent.getCorrelationId());
    }

    private record RegistrationContext(User user, UUID correlationId) {}
}
