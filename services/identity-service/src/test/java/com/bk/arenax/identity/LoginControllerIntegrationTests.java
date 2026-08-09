package com.bk.arenax.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.domain.RefreshSession;
import com.bk.arenax.identity.domain.User;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerIntegrationTests {

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

    @Autowired
    private UserIdentifierRepository userIdentifierRepository;

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
    void loginReturnsAccessTokenAndStoresRefreshSessionHash() throws Exception {
        UUID userId = registerAndVerifyUser();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev",
                                  "password": "Sup3rSecret!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("arenax_refresh_token"))
                .andExpect(cookie().httpOnly("arenax_refresh_token", true))
                .andExpect(cookie().path("arenax_refresh_token", "/api/v1/auth"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.userId").value(userId.toString()))
                .andExpect(jsonPath("$.user.email").value("player1@arenax.dev"))
                .andExpect(jsonPath("$.user.fullName").value("Player One"))
                .andExpect(jsonPath("$.user.status").value("ACTIVE"))
                .andExpect(jsonPath("$.user.roles").isArray())
                .andExpect(jsonPath("$.user.permissions").isArray())
                .andReturn();

        JsonNode responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        String accessToken = responseBody.path("accessToken").asText();
        Jwt jwt = jwtDecoder.decode(accessToken);

        List<RefreshSession> refreshSessions = refreshSessionRepository.findAll();
        assertThat(refreshSessions).hasSize(1);

        RefreshSession refreshSession = refreshSessions.getFirst();
        assertThat(refreshSession.getUserId()).isEqualTo(userId);
        assertThat(refreshSession.getTokenHash()).isNotBlank();
        assertThat(refreshSession.getExpiresAt()).isNotNull();
        assertThat(refreshSession.getRevokedAt()).isNull();

        assertThat(jwt.getSubject()).isEqualTo(userId.toString());
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("arenax-identity");
        assertThat(jwt.getAudience()).containsExactly("arenax-api");
        assertThat(jwt.getClaimAsString("sid")).isEqualTo(refreshSession.getId().toString());
        assertThat(((Number) jwt.getClaim("token_version")).intValue()).isZero();
        assertThat(jwt.getClaimAsStringList("roles")).isEmpty();
        assertThat(jwt.getClaimAsStringList("permissions")).isEmpty();

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    void loginRejectsInvalidCredentialsWithGenericUnauthorizedResponse() throws Exception {
        UUID userId = registerAndVerifyUser();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev",
                                  "password": "WrongPassword!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void loginLocksUserAfterFiveFailedAttempts() throws Exception {
        UUID userId = registerAndVerifyUser();

        for (int attempt = 1; attempt <= 4; attempt++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "player1@arenax.dev",
                                      "password": "WrongPassword!"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev",
                                  "password": "WrongPassword!"
                                }
                                """))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"))
                .andExpect(jsonPath("$.message").value("Account is temporarily locked"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev",
                                  "password": "Sup3rSecret!"
                                }
                                """))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isAfter(java.time.Instant.now());
    }

    @Test
    void loginAsUnverifiedUserReturnsPendingProfile() throws Exception {
        registerUser();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev",
                                  "password": "Sup3rSecret!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("arenax_refresh_token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.status").value("PENDING"))
                .andExpect(jsonPath("$.user.emailVerifiedAt").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void loginRejectsSuspendedAccountWithForbidden() throws Exception {
        UUID userId = registerAndVerifyUser();
        User user = userRepository.findById(userId).orElseThrow();
        user.suspend();
        userRepository.save(user);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev",
                                  "password": "Sup3rSecret!"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_SUSPENDED"))
                .andExpect(jsonPath("$.message").value("Account is suspended"));
    }

    @Test
    void loginRejectsDeactivatedAccountWithForbidden() throws Exception {
        UUID userId = registerAndVerifyUser();
        User user = userRepository.findById(userId).orElseThrow();
        user.deactivate();
        userRepository.save(user);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player1@arenax.dev",
                                  "password": "Sup3rSecret!"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_DEACTIVATED"))
                .andExpect(jsonPath("$.message").value("Account is deactivated"));
    }

    private UUID registerAndVerifyUser() throws Exception {
        UUID userId = registerUser();

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

    private UUID registerUser() throws Exception {
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

        return UUID.fromString(objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .path("userId")
                .asText());
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
