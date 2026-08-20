package com.bk.arenax.tenant.controller;

import com.bk.arenax.tenant.dto.response.AccountSummaryResponse;
import com.bk.arenax.tenant.dto.request.CreateWorkspaceRequest;
import com.bk.arenax.tenant.dto.response.MembershipResponse;
import com.bk.arenax.tenant.infrastructure.security.CurrentUserResolver;
import com.bk.arenax.tenant.infrastructure.security.GatewayUserPrincipal;
import com.bk.arenax.tenant.service.AccountService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final CurrentUserResolver currentUserResolver;

    public AccountController(AccountService accountService, CurrentUserResolver currentUserResolver) {
        this.accountService = accountService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public List<AccountSummaryResponse> myAccounts(Authentication authentication) {
        GatewayUserPrincipal principal = currentUserResolver.resolve(authentication);
        return accountService.listAccounts(principal.userId(), principal.accountId());
    }

    @PostMapping("/workspaces")
    public ResponseEntity<AccountSummaryResponse> createWorkspace(
            Authentication authentication, @Valid @RequestBody CreateWorkspaceRequest request) {
        GatewayUserPrincipal principal = currentUserResolver.resolve(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createWorkspace(principal.userId(), request.name()));
    }

    @GetMapping("/{accountId}/memberships")
    public List<MembershipResponse> memberships(
            Authentication authentication, @PathVariable UUID accountId) {
        GatewayUserPrincipal principal = currentUserResolver.resolve(authentication);
        return accountService.listMemberships(principal.userId(), accountId);
    }
}
