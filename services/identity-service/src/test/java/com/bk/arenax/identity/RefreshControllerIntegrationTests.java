package com.bk.arenax.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class RefreshControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtDecoder jwtDecoder;

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
    void refreshRotatesRefreshSessionAndReturnsNewAccessToken() throws Exception {
        UUID userId = registerAndVerifyUser();
        Cookie originalRefreshCookie = loginUser();

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(originalRefreshCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("arenax_refresh_token"))
                .andExpect(cookie().httpOnly("arenax_refresh_token", true))
                .andExpect(cookie().path("arenax_refresh_token", "/api/v1/auth"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.userId").value(userId.toString()))
                .andReturn();

        Cookie rotatedRefreshCookie = refreshResult.getResponse().getCookie("arenax_refresh_token");
        assertThat(rotatedRefreshCookie).isNotNull();
        assertThat(rotatedRefreshCookie.getValue()).isNotEqualTo(originalRefreshCookie.getValue());

        JsonNode responseBody = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        Jwt jwt = jwtDecoder.decode(responseBody.path("accessToken").asText());

        List<RefreshSession> refreshSessions = refreshSessionRepository.findAll().stream()
                .sorted(Comparator.comparing(RefreshSession::getCreatedAt))
                .toList();
        assertThat(refreshSessions).hasSize(2);
        assertThat(refreshSessions.getFirst().getRevokedAt()).isNotNull();
        assertThat(refreshSessions.get(1).getRevokedAt()).isNull();
        assertThat(refreshSessions.get(1).getUserId()).isEqualTo(userId);
        assertThat(jwt.getSubject()).isEqualTo(userId.toString());
        assertThat(jwt.getClaimAsString("sid")).isEqualTo(refreshSessions.get(1).getId().toString());
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

        return loginResult.getResponse().getCookie("arenax_refresh_token");
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
