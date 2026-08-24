package com.bk.arenax.tenant.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bk.arenax.tenant.domain.entity.Account;
import com.bk.arenax.tenant.domain.entity.Membership;
import com.bk.arenax.tenant.domain.enums.AccountStatus;
import com.bk.arenax.tenant.domain.enums.AccountType;
import com.bk.arenax.tenant.domain.enums.MembershipRole;
import com.bk.arenax.tenant.dto.response.AccountSummaryResponse;
import com.bk.arenax.tenant.dto.response.MembershipResponse;
import com.bk.arenax.tenant.repository.AccountRepository;
import com.bk.arenax.tenant.repository.MembershipRepository;

@Service
public class AccountService {

  private final AccountRepository accountRepository;
  private final MembershipRepository membershipRepository;

  public AccountService(
      AccountRepository accountRepository, MembershipRepository membershipRepository) {
    this.accountRepository = accountRepository;
    this.membershipRepository = membershipRepository;
  }

  @Transactional(readOnly = true)
  public List<AccountSummaryResponse> listAccounts(UUID userId, UUID currentAccountId) {
    List<Membership> memberships = membershipRepository.findAllByUserIdOrderByCreatedAtAsc(userId);
    Map<UUID, Account> accountsById =
        accountRepository
            .findAllById(memberships.stream().map(Membership::getAccountId).toList())
            .stream()
            .collect(Collectors.toMap(Account::getId, account -> account));

    return memberships.stream()
        .map(
            membership ->
                toAccountSummary(
                    accountsById.get(membership.getAccountId()), membership, currentAccountId))
        .filter(Objects::nonNull)
        .sorted(Comparator.comparing(AccountSummaryResponse::name))
        .toList();
  }

  @Transactional
  public AccountSummaryResponse createWorkspace(UUID userId, String name) {
    String normalizedName = normalizeName(name);

    Account account = new Account();
    account.setOwnerUserId(userId);
    account.setName(normalizedName);
    account.setType(AccountType.TEAM);
    account.setStatus(AccountStatus.ACTIVE);
    accountRepository.save(account);

    Membership membership = new Membership();
    membership.setAccountId(account.getId());
    membership.setUserId(userId);
    membership.setRole(MembershipRole.OWNER);
    membershipRepository.save(membership);

    return toAccountSummary(account, membership, account.getId());
  }

  @Transactional(readOnly = true)
  public List<MembershipResponse> listMemberships(UUID requestingUserId, UUID accountId) {
    if (membershipRepository.findByAccountIdAndUserId(accountId, requestingUserId).isEmpty()) {
      throw new AccessDeniedException("You do not belong to this account");
    }
    return membershipRepository.findAllByAccountIdOrderByCreatedAtAsc(accountId).stream()
        .map(
            membership ->
                new MembershipResponse(
                    membership.getId(), membership.getUserId(), membership.getRole().name()))
        .toList();
  }

  private AccountSummaryResponse toAccountSummary(
      Account account, Membership membership, UUID currentAccountId) {
    if (account == null || membership == null) {
      return null;
    }
    return new AccountSummaryResponse(
        account.getId(),
        account.getName(),
        account.getType().name(),
        account.getStatus().name(),
        membership.getRole().name(),
        account.getId().equals(currentAccountId));
  }

  private String normalizeName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Workspace name must not be blank");
    }
    return name.trim();
  }
}
