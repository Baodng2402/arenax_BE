package com.bk.arenax.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.domain.PasswordResetToken;
import com.bk.arenax.identity.domain.RefreshSession;
import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.repository.EmailVerificationTokenRepository;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.identity.repository.PasswordResetTokenRepository;
import com.bk.arenax.identity.repository.RefreshSessionRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

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
    void requestPasswordResetReturnsAcceptedAndStoresConfidentialResetTokenForExistingUser() throws Exception {
        UUID userId = registerAndVerifyUser();

        mockMvc.perform(post("/api/v1/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev"
                                }
                                """))
                .andExpect(status().isAccepted());

        List<PasswordResetToken> resetTokens = passwordResetTokenRepository.findAll();
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();

        assertThat(resetTokens).hasSize(1);
        assertThat(resetTokens.getFirst().getUserId()).isEqualTo(userId);
        assertThat(resetTokens.getFirst().getTokenHash()).isNotBlank();
        assertThat(resetTokens.getFirst().getExpiresAt()).isNotNull();
        assertThat(outboxEvents).extracting(OutboxEvent::getEventType)
                .contains("identity.user.password-reset-requested.v1");

        OutboxEvent resetEvent = outboxEvents.stream()
                .filter(event -> event.getEventType().equals("identity.user.password-reset-requested.v1"))
                .findFirst()
                .orElseThrow();
        JsonNode payload = objectMapper.readTree(resetEvent.getPayload()).path("payload");
        assertThat(payload.path("userId").asText()).isEqualTo(userId.toString());
        assertThat(payload.path("email").asText()).isEqualTo("player1@arenax.dev");
        assertThat(payload.path("displayName").asText()).isEqualTo("Player One");
        assertThat(payload.path("resetToken").asText()).isNotBlank();
        assertThat(payload.path("resetToken").asText()).isNotEqualTo(resetTokens.getFirst().getTokenHash());
    }

    @Test
    void requestPasswordResetReturnsAcceptedWithoutLeakingUnknownEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "missing@arenax.dev"
                                }
                                """))
                .andExpect(status().isAccepted());

        assertThat(passwordResetTokenRepository.findAll()).isEmpty();
        assertThat(outboxEventRepository.findAll()).isEmpty();
    }

    @Test
    void resetPasswordChangesPasswordRevokesSessionsAndConsumesToken() throws Exception {
        UUID userId = registerAndVerifyUser();
        Cookie refreshCookie = loginUser("player1@arenax.dev", "Sup3rSecret!");

        mockMvc.perform(post("/api/v1/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev"
                                }
                                """))
                .andExpect(status().isAccepted());

        String resetToken = extractToken("identity.user.password-reset-requested.v1", "resetToken");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s",
                                  "newPassword": "An0therSecret!"
                                }
                                """.formatted(resetToken)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().value("arenax_refresh_token", ""))
                .andExpect(cookie().maxAge("arenax_refresh_token", 0));

        User user = userRepository.findById(userId).orElseThrow();
        List<PasswordResetToken> resetTokens = passwordResetTokenRepository.findAll();
        List<RefreshSession> refreshSessions = refreshSessionRepository.findAll();

        assertThat(user.getTokenVersion()).isEqualTo(1);
        assertThat(user.getPasswordChangedAt()).isNotNull();
        assertThat(resetTokens).hasSize(1);
        assertThat(resetTokens.getFirst().getConsumedAt()).isNotNull();
        assertThat(refreshSessions).hasSize(1);
        assertThat(refreshSessions.getFirst().getRevokedAt()).isNotNull();

        assertAuthenticates("player1@arenax.dev", "An0therSecret!");
        assertRejectsAuthentication("player1@arenax.dev", "Sup3rSecret!");
    }

    @Test
    void resetPasswordRejectsUnknownTokenAsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "abcdefghijklmnopqrstuvwxyzABCDEF",
                                  "newPassword": "An0therSecret!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void resetPasswordRejectsConsumedTokenAsGone() throws Exception {
        registerAndVerifyUser();

        mockMvc.perform(post("/api/v1/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev"
                                }
                                """))
                .andExpect(status().isAccepted());

        String resetToken = extractToken("identity.user.password-reset-requested.v1", "resetToken");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s",
                                  "newPassword": "An0therSecret!"
                                }
                                """.formatted(resetToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s",
                                  "newPassword": "Th1rdSecret!"
                                }
                                """.formatted(resetToken)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("TOKEN_NO_LONGER_VALID"));
    }

    private UUID registerAndVerifyUser() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
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

        UUID userId = UUID.fromString(objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .path("userId")
                .asText());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s"
                                }
                                """.formatted(extractToken("identity.user.verification-requested.v1", "verificationToken"))))
                .andExpect(status().isNoContent());

        return userId;
    }

    private Cookie loginUser(String email, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return loginResult.getResponse().getCookie("arenax_refresh_token");
    }

    private String extractToken(String eventType, String tokenField) throws Exception {
        OutboxEvent event = outboxEventRepository.findAll().stream()
                .filter(candidate -> candidate.getEventType().equals(eventType))
                .reduce((first, second) -> second)
                .orElseThrow();
        return objectMapper.readTree(event.getPayload()).path("payload").path(tokenField).asText();
    }

    private void assertAuthenticates(String email, String password) {
        assertThat(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)).isAuthenticated()).isTrue();
    }

    private void assertRejectsAuthentication(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException exception) {
            return;
        } catch (AuthenticationException exception) {
            return;
        }
        throw new AssertionError("Expected authentication to fail");
    }
}
