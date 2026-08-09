package com.bk.arenax.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bk.arenax.tenant.domain.entity.Account;
import com.bk.arenax.tenant.domain.entity.Membership;
import com.bk.arenax.tenant.domain.enums.AccountStatus;
import com.bk.arenax.tenant.domain.enums.AccountType;
import com.bk.arenax.tenant.domain.enums.MembershipRole;
import com.bk.arenax.tenant.repository.AccountRepository;
import com.bk.arenax.tenant.repository.MembershipRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @BeforeEach
    void setUp() {
        membershipRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void myAccountsReturnsMembershipsForCurrentUser() throws Exception {
        UUID userId = UUID.fromString("77ef4244-8b4d-4f0a-9415-440dd06ccb6c");
        Account personal = account("Player One", userId, AccountType.PERSONAL);
        accountRepository.save(personal);
        membershipRepository.save(membership(personal.getId(), userId, MembershipRole.OWNER));

        Account team = account("Arena Ops", userId, AccountType.TEAM);
        accountRepository.save(team);
        membershipRepository.save(membership(team.getId(), userId, MembershipRole.OWNER));

        mockMvc.perform(trusted(get("/api/v1/accounts"), userId, team.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Arena Ops"))
                .andExpect(jsonPath("$[0].type").value("TEAM"))
                .andExpect(jsonPath("$[0].current").value(true))
                .andExpect(jsonPath("$[1].name").value("Player One"))
                .andExpect(jsonPath("$[1].type").value("PERSONAL"));
    }

    @Test
    void createWorkspaceCreatesAccountAndOwnerMembership() throws Exception {
        UUID userId = UUID.fromString("f5770ded-e304-4f1b-89ec-670bbcc677bc");

        mockMvc.perform(trusted(post("/api/v1/accounts/workspaces"), userId, null)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(java.util.Map.of("name", "Arena Ops"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Arena Ops"))
                .andExpect(jsonPath("$.type").value("TEAM"))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.current").value(true));

        assertThat(accountRepository.findAll()).hasSize(1);
        assertThat(accountRepository.findAll().getFirst().getOwnerUserId()).isEqualTo(userId);
        assertThat(membershipRepository.findAll()).hasSize(1);
        assertThat(membershipRepository.findAll().getFirst().getRole()).isEqualTo(MembershipRole.OWNER);
    }

    @Test
    void membershipsReturnsMembersForAuthorizedUser() throws Exception {
        UUID ownerUserId = UUID.fromString("9d2ef0cc-b245-465f-97b0-25814f10da27");
        UUID memberUserId = UUID.fromString("2077738f-44ab-4fe8-8429-d8d08d4cc4f8");
        Account team = account("Arena Ops", ownerUserId, AccountType.TEAM);
        accountRepository.save(team);
        membershipRepository.save(membership(team.getId(), ownerUserId, MembershipRole.OWNER));
        membershipRepository.save(membership(team.getId(), memberUserId, MembershipRole.MEMBER));

        mockMvc.perform(trusted(get("/api/v1/accounts/{accountId}/memberships", team.getId()), ownerUserId, team.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("OWNER"))
                .andExpect(jsonPath("$[1].role").value("MEMBER"));
    }

    @Test
    void membershipsRejectsNonMember() throws Exception {
        UUID ownerUserId = UUID.fromString("bf252319-b9b6-4b8b-8f2a-ff0e343d70fb");
        UUID outsiderUserId = UUID.fromString("21e80ca6-e604-4875-a99a-aec93b26f2ce");
        Account team = account("Arena Ops", ownerUserId, AccountType.TEAM);
        accountRepository.save(team);
        membershipRepository.save(membership(team.getId(), ownerUserId, MembershipRole.OWNER));

        mockMvc.perform(trusted(get("/api/v1/accounts/{accountId}/memberships", team.getId()), outsiderUserId, null))
                .andExpect(status().isForbidden());
    }

    private Account account(String name, UUID ownerUserId, AccountType type) {
        Account account = new Account();
        account.setOwnerUserId(ownerUserId);
        account.setName(name);
        account.setType(type);
        account.setStatus(AccountStatus.ACTIVE);
        return account;
    }

    private Membership membership(UUID accountId, UUID userId, MembershipRole role) {
        Membership membership = new Membership();
        membership.setAccountId(accountId);
        membership.setUserId(userId);
        membership.setRole(role);
        return membership;
    }

    private MockHttpServletRequestBuilder trusted(
            MockHttpServletRequestBuilder builder, UUID userId, UUID accountId) {
        builder.header("X-Arenax-User-Id", userId.toString());
        if (accountId != null) {
            builder.header("X-Arenax-Account-Id", accountId.toString());
        }
        return builder;
    }
}
