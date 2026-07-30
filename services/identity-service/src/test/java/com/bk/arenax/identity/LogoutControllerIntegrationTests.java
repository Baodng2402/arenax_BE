package com.bk.arenax.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.domain.RefreshSession;
import com.bk.arenax.identity.repository.EmailVerificationTokenRepository;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.identity.repository.PasswordResetTokenRepository;
import com.bk.arenax.identity.repository.RefreshSessionRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class LogoutControllerIntegrationTests {

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
    private RefreshSessionRepository refreshSessionRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        refreshSessionRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void logoutRevokesCurrentRefreshSessionAndClearsCookie() throws Exception {
        registerAndVerifyUser();
        Cookie refreshCookie = loginUser();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().value("arenax_refresh_token", ""))
                .andExpect(cookie().maxAge("arenax_refresh_token", 0))
                .andExpect(cookie().path("arenax_refresh_token", "/api/v1/auth"));

        List<RefreshSession> refreshSessions = refreshSessionRepository.findAll();
        assertThat(refreshSessions).hasSize(1);
        assertThat(refreshSessions.getFirst().getRevokedAt()).isNotNull();
    }

    @Test
    void logoutAllRevokesEveryRefreshSessionForAuthenticatedUserAndClearsCookie() throws Exception {
        UUID userId = registerAndVerifyUser();
        LoginArtifacts firstLogin = loginUserWithAccessToken();
        loginUser();

        mockMvc.perform(post("/api/v1/auth/logout-all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + firstLogin.accessToken())
                        .cookie(firstLogin.refreshCookie()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().value("arenax_refresh_token", ""))
                .andExpect(cookie().maxAge("arenax_refresh_token", 0))
                .andExpect(cookie().path("arenax_refresh_token", "/api/v1/auth"));

        List<RefreshSession> refreshSessions = refreshSessionRepository.findAll().stream()
                .sorted(Comparator.comparing(RefreshSession::getCreatedAt))
                .toList();
        assertThat(refreshSessions).hasSize(2);
        assertThat(refreshSessions).allMatch(session -> session.getUserId().equals(userId));
        assertThat(refreshSessions).allMatch(session -> session.getRevokedAt() != null);
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

        String verificationToken = extractVerificationToken(outboxEventRepository.findAll());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s"
                                }
                                """.formatted(verificationToken)))
                .andExpect(status().isNoContent());

        return userId;
    }

    private Cookie loginUser() throws Exception {
        return loginUserWithAccessToken().refreshCookie();
    }

    private LoginArtifacts loginUserWithAccessToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev",
                                  "password": "Sup3rSecret!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("arenax_refresh_token");
        JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return new LoginArtifacts(refreshCookie, body.path("accessToken").asText());
    }

    private String extractVerificationToken(List<OutboxEvent> outboxEvents) throws Exception {
        OutboxEvent verificationEvent = outboxEvents.stream()
                .filter(event -> event.getEventType().equals("identity.user.verification-requested.v1"))
                .findFirst()
                .orElseThrow();
        JsonNode payload = objectMapper.readTree(verificationEvent.getPayload());
        return payload.path("payload").path("verificationToken").asText();
    }

    private record LoginArtifacts(Cookie refreshCookie, String accessToken) {
    }
}
