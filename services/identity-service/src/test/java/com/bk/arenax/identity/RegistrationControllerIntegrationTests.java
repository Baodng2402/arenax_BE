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
class RegistrationControllerIntegrationTests {

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
    void registerCreatesPendingUserWithNormalizedEmail() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "  Player1@ArenaX.dev ",
                                  "password": "Sup3rSecret!",
                                  "fullName": "Player One"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("player1@arenax.dev"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID userId = UUID.fromString(body.path("userId").asText());

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getEmail()).isEqualTo("player1@arenax.dev");
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(user.getPasswordHash()).isNotEqualTo("Sup3rSecret!");
        assertThat(user.getPasswordHash()).startsWith("$2");

        List<EmailVerificationToken> verificationTokens = emailVerificationTokenRepository.findAll();
        assertThat(verificationTokens).hasSize(1);
        assertThat(verificationTokens.getFirst().getUserId()).isEqualTo(userId);
        assertThat(verificationTokens.getFirst().getTokenHash()).isNotBlank();
        assertThat(verificationTokens.getFirst().getExpiresAt()).isNotNull();

        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents).hasSize(1);
        assertThat(outboxEvents.getFirst().getEventType()).isEqualTo("identity.user.verification-requested.v1");
        assertThat(outboxEvents.getFirst().getCorrelationId()).isEqualTo(userId);

        JsonNode eventPayload = objectMapper.readTree(outboxEvents.getFirst().getPayload());
        assertThat(eventPayload.path("payload").path("userId").asText()).isEqualTo(userId.toString());
        assertThat(eventPayload.path("payload").path("email").asText()).isEqualTo("player1@arenax.dev");
        assertThat(eventPayload.path("payload").path("displayName").asText()).isEqualTo("Player One");
        assertThat(eventPayload.path("payload").path("verificationToken").asText()).isNotBlank();
        assertThat(eventPayload.path("payload").path("verificationToken").asText())
                .isNotEqualTo(verificationTokens.getFirst().getTokenHash());
    }
}
