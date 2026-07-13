package com.bk.arenax.identity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.bk.arenax.identity.domain.entity.AuthorizationProjection;
import com.bk.arenax.identity.domain.entity.User;
import com.bk.arenax.identity.domain.enums.UserStatus;
import com.bk.arenax.identity.repository.AuthorizationProjectionRepository;
import com.bk.arenax.identity.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorizationProjectionRepository authorizationProjectionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        authorizationProjectionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerCreatesProvisioningUser() throws Exception {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("email", "player1@arenax.dev");
        request.put("password", "secret123");
        request.put("displayName", "Player One");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("player1@arenax.dev"))
                .andExpect(jsonPath("$.displayName").value("Player One"))
                .andExpect(jsonPath("$.status").value("PROVISIONING"));
    }

    @Test
    void loginRejectsProvisioningUser() throws Exception {
        ObjectNode registerRequest = objectMapper.createObjectNode();
        registerRequest.put("email", "player1@arenax.dev");
        registerRequest.put("password", "secret123");
        registerRequest.put("displayName", "Player One");

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(registerRequest)));

        ObjectNode loginRequest = objectMapper.createObjectNode();
        loginRequest.put("email", "player1@arenax.dev");
        loginRequest.put("password", "secret123");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User onboarding is not complete"));
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        ObjectNode request = objectMapper.createObjectNode()
                .put("email", "player1@arenax.dev")
                .put("password", "secret123")
                .put("displayName", "Player One");

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void loginReturnsAccessTokenForActiveUser() throws Exception {
        User user = new User();
        user.setEmail("active@arenax.dev");
        user.setPasswordHash(passwordEncoder.encode("secret123"));
        user.setDisplayName("Active Player");
        user.setStatus(UserStatus.ACTIVE);
        user.setActiveAccountId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        userRepository.save(user);

        AuthorizationProjection projection = new AuthorizationProjection();
        projection.setUserId(user.getId());
        projection.setAccountId(user.getActiveAccountId());
        projection.setRoles(List.of("USER"));
        projection.setPermissions(List.of("MATCH:CREATE", "MATCH:JOIN"));
        authorizationProjectionRepository.save(projection);

        ObjectNode loginRequest = objectMapper.createObjectNode();
        loginRequest.put("email", "active@arenax.dev");
        loginRequest.put("password", "secret123");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value(""))
                .andExpect(jsonPath("$.expiresInSeconds").value(900));
    }
}
