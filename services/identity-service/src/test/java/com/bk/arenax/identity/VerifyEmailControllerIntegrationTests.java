package com.bk.arenax.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bk.arenax.identity.domain.EmailVerificationToken;
import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.domain.UserStatus;
import com.bk.arenax.identity.repository.EmailVerificationTokenRepository;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.identity.repository.PasswordResetTokenRepository;
import com.bk.arenax.identity.repository.RefreshSessionRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class VerifyEmailControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RefreshSessionRepository refreshSessionRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        refreshSessionRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void verifyEmailActivatesPendingUserAndPublishesRegisteredEvent() throws Exception {
        UUID userId = registerPendingUser();
        String verificationToken = extractVerificationToken(outboxEventRepository.findAll());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s"
                                }
                                """.formatted(verificationToken)))
                .andExpect(status().isNoContent());

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmailVerifiedAt()).isNotNull();

        List<EmailVerificationToken> verificationTokens = emailVerificationTokenRepository.findAll();
        assertThat(verificationTokens).hasSize(1);
        assertThat(verificationTokens.getFirst().getConsumedAt()).isNotNull();

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents).hasSize(2);

        OutboxEvent registeredEvent = outboxEvents.stream()
                .filter(event -> event.getEventType().equals("identity.user.registered.v1"))
                .findFirst()
                .orElseThrow();

        JsonNode payload = objectMapper.readTree(registeredEvent.getPayload());
        assertThat(payload.path("payload").path("userId").asText()).isEqualTo(userId.toString());
        assertThat(payload.path("payload").path("email").asText()).isEqualTo("player1@arenax.dev");
        assertThat(payload.path("payload").path("displayName").asText()).isEqualTo("Player One");
    }

    @Test
    void verifyEmailRejectsUnknownTokenAsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "abcdefghijklmnopqrstuvwxyzABCDEF"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void verifyEmailRejectsConsumedTokenAsGone() throws Exception {
        registerPendingUser();
        String verificationToken = extractVerificationToken(outboxEventRepository.findAll());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s"
                                }
                                """.formatted(verificationToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s"
                                }
                                """.formatted(verificationToken)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("TOKEN_NO_LONGER_VALID"));
    }

    private UUID registerPendingUser() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev",
                                  "password": "Sup3rSecret!",
                                  "fullName": "Player One"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(responseBody.path("userId").asText());
    }

    private String extractVerificationToken(List<OutboxEvent> outboxEvents) throws Exception {
        OutboxEvent verificationEvent = outboxEvents.stream()
                .filter(event -> event.getEventType().equals("identity.user.verification-requested.v1"))
                .findFirst()
                .orElseThrow();
        JsonNode payload = objectMapper.readTree(verificationEvent.getPayload());
        return payload.path("payload").path("verificationToken").asText();
    }
}
