package com.bk.arenax.dto.response;

import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.user.User;
import java.util.Set;
import java.util.stream.Collectors;

public record UserRolesResponse(Long userId, Long accountId, Set<String> roleCodeNames) {
  public static UserRolesResponse from(User user, Account account) {
    return new UserRolesResponse(
        user.getId(),
        account.getId(),
        user.rolesForAccount(account.getId()).stream()
            .map(role -> role.getCodeName())
            .collect(Collectors.toSet()));
  }
}
