package com.bk.arenax.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.domain.UserIdentifier;
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
class UserControllerIntegrationTests {

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
    void meReturnsProfileWhenTrustedGatewayHeadersPresent() throws Exception {
        UUID userId = registerAndVerifyUser();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-Arenax-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.primaryEmail").value("player1@arenax.dev"))
                .andExpect(jsonPath("$.emails[0].email").value("player1@arenax.dev"))
                .andExpect(jsonPath("$.emails[0].primary").value(true))
                .andExpect(jsonPath("$.emails[0].verified").value(true))
                .andExpect(jsonPath("$.fullName").value("Player One"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.permissions").isArray());
    }

    @Test
    void meReturnsUnauthorizedWithoutTrustedHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsUnauthorizedForMalformedTrustedUserIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-Arenax-User-Id", "not-a-uuid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsProfileWithBearerAccessToken() throws Exception {
        UUID userId = registerAndVerifyUser();

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

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("accessToken")
                .asText();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.primaryEmail").value("player1@arenax.dev"))
                .andExpect(jsonPath("$.fullName").value("Player One"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void meReturnsNotFoundForUnknownUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-Arenax-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void updateMeUpdatesFullNameAndAvatarUrl() throws Exception {
        UUID userId = registerAndVerifyUser();

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("X-Arenax-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "New Name",
                                  "avatarUrl": "https://cdn.example.com/avatar.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("New Name"))
                .andExpect(jsonPath("$.avatarUrl").value("https://cdn.example.com/avatar.png"));

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getFullName()).isEqualTo("New Name");
        assertThat(user.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar.png");
    }

    @Test
    void updateMeRejectsBlankFullName() throws Exception {
        UUID userId = registerAndVerifyUser();

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("X-Arenax-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void putUsernameSetsPublicHandleAndDeleteClearsIt() throws Exception {
        UUID userId = registerAndVerifyUser();

        mockMvc.perform(put("/api/v1/users/me/username")
                        .header("X-Arenax-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ArenaMaster"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("arenamaster"));

        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-Arenax-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("arenamaster"));

        mockMvc.perform(delete("/api/v1/users/me/username")
                        .header("X-Arenax-User-Id", userId.toString()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-Arenax-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").doesNotExist());
    }

    @Test
    void putUsernameRejectsUsernameAlreadyTakenByAnotherUser() throws Exception {
        UUID firstUser = registerAndVerifyUser();

        mockMvc.perform(put("/api/v1/users/me/username")
                        .header("X-Arenax-User-Id", firstUser.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ArenaMaster"
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult secondRegister = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player2@arenax.dev",
                                  "password": "Sup3rSecret!",
                                  "fullName": "Player Two"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        UUID secondUser = UUID.fromString(objectMapper.readTree(secondRegister.getResponse().getContentAsString())
                .path("userId")
                .asText());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s"
                                }
                                """.formatted(extractVerificationToken(outboxEventRepository.findAll()))))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/v1/users/me/username")
                        .header("X-Arenax-User-Id", secondUser.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "arenamaster"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void putUsernameIsIdempotentWhenReSettingOwnUsername() throws Exception {
        UUID userId = registerAndVerifyUser();

        mockMvc.perform(put("/api/v1/users/me/username")
                        .header("X-Arenax-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ArenaMaster"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/users/me/username")
                        .header("X-Arenax-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "arenamaster"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("arenamaster"));
    }

    @Test
    void putUsernameRejectsTooShortUsername() throws Exception {
        UUID userId = registerAndVerifyUser();

        mockMvc.perform(put("/api/v1/users/me/username")
                        .header("X-Arenax-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ab"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void addEmailCreatesSecondaryIdentifierAndSetPrimaryRequiresVerifiedEmail() throws Exception {
        UUID userId = registerAndVerifyUser();
        outboxEventRepository.deleteAll();

        String response = mockMvc.perform(post("/api/v1/users/me/emails")
                        .header("X-Arenax-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "Second@ArenaX.dev"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("second@arenax.dev"))
                .andExpect(jsonPath("$.primary").value(false))
                .andExpect(jsonPath("$.verified").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode emailBody = objectMapper.readTree(response);
        String emailId = emailBody.path("id").asText();

        assertThat(userIdentifierRepository.findAll()).hasSize(2);
        OutboxEvent verificationEvent = outboxEventRepository.findAll().getFirst();
        assertThat(verificationEvent.getEventType()).isEqualTo("identity.user.verification-requested.v1");
        assertThat(objectMapper.readTree(verificationEvent.getPayload()).path("payload").path("email").asText())
                .isEqualTo("second@arenax.dev");

        mockMvc.perform(patch("/api/v1/users/me/emails/{emailId}/primary", emailId)
                        .header("X-Arenax-User-Id", userId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void verifiedSecondaryEmailCanBecomePrimaryAndOldPrimaryCannotBeDeletedUntilSwitched() throws Exception {
        UUID userId = registerAndVerifyUser();
        outboxEventRepository.deleteAll();

        String response = mockMvc.perform(post("/api/v1/users/me/emails")
                        .header("X-Arenax-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "Second@ArenaX.dev"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String emailId = objectMapper.readTree(response).path("id").asText();

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s"
                                }
                                """.formatted(extractVerificationToken(outboxEventRepository.findAll()))))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/v1/users/me/emails/{emailId}/primary", emailId)
                        .header("X-Arenax-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryEmail").value("second@arenax.dev"))
                .andExpect(jsonPath("$.emails[0].email").value("second@arenax.dev"))
                .andExpect(jsonPath("$.emails[0].primary").value(true));

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getEmail()).isEqualTo("second@arenax.dev");

        UserIdentifier originalPrimary = userIdentifierRepository.findAll().stream()
                .filter(identifier -> identifier.getNormalizedValue().equals("player1@arenax.dev"))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/api/v1/users/me/emails/{emailId}", originalPrimary.getId())
                        .header("X-Arenax-User-Id", userId.toString()))
                .andExpect(status().isNoContent());

        assertThat(userIdentifierRepository.findAll()).hasSize(1);
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

    private String extractVerificationToken(List<OutboxEvent> outboxEvents) throws Exception {
        OutboxEvent verificationEvent = outboxEvents.stream()
                .filter(event -> event.getEventType().equals("identity.user.verification-requested.v1"))
                .reduce((first, second) -> second)
                .orElseThrow();
        JsonNode payload = objectMapper.readTree(verificationEvent.getPayload());
        return payload.path("payload").path("verificationToken").asText();
    }
}
