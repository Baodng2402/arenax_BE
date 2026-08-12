package com.bk.arenax.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.domain.UserIdentifier;
import com.bk.arenax.identity.domain.UserIdentifierType;
import com.bk.arenax.identity.repository.EmailVerificationTokenRepository;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.identity.repository.PasswordResetTokenRepository;
import com.bk.arenax.identity.repository.RefreshSessionRepository;
import com.bk.arenax.identity.repository.UserIdentifierRepository;
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
class UserIdentifierIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserIdentifierRepository userIdentifierRepository;

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
        userIdentifierRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerCreatesPrimaryEmailIdentifier() throws Exception {
        UUID userId = registerUser("  Player1@ArenaX.dev ");

        List<UserIdentifier> identifiers = userIdentifierRepository.findAll();
        assertThat(identifiers).hasSize(1);

        UserIdentifier identifier = identifiers.getFirst();
        assertThat(identifier.getUserId()).isEqualTo(userId);
        assertThat(identifier.getType()).isEqualTo(UserIdentifierType.EMAIL);
        assertThat(identifier.getNormalizedValue()).isEqualTo("player1@arenax.dev");
        assertThat(identifier.isPrimary()).isTrue();
        assertThat(identifier.getVerifiedAt()).isNull();
    }

    @Test
    void verifyEmailMarksPrimaryIdentifierVerified() throws Exception {
        registerUser("player1@arenax.dev");

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s"
                                }
                                """.formatted(extractVerificationToken())))
                .andExpect(status().isNoContent());

        UserIdentifier identifier = userIdentifierRepository.findAll().getFirst();
        assertThat(identifier.getVerifiedAt()).isNotNull();
    }

    private UUID registerUser(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Sup3rSecret!",
                                  "fullName": "Player One"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .path("userId")
                .asText());
    }

    private String extractVerificationToken() throws Exception {
        OutboxEvent verificationEvent = outboxEventRepository.findAll().stream()
                .filter(event -> event.getEventType().equals("identity.user.verification-requested.v1"))
                .findFirst()
                .orElseThrow();
        JsonNode payload = objectMapper.readTree(verificationEvent.getPayload());
        return payload.path("payload").path("verificationToken").asText();
    }
}
