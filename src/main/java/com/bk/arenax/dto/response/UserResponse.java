package com.bk.arenax.dto.response;

import com.bk.arenax.domain.account.AccountStatus;
import com.bk.arenax.domain.account.AccountType;
import com.bk.arenax.domain.subscription.SubscriptionPlan;
import com.bk.arenax.domain.subscription.SubscriptionStatus;
import com.bk.arenax.domain.user.Gender;
import com.bk.arenax.domain.user.UserRole;
import com.bk.arenax.domain.user.UserStatus;
import java.time.Instant;

public record UserResponse(
    Long id,
    String name,
    String fullName,
    String displayName,
    String phoneNumber,
    String avatarUrl,
    String email,
    Gender gender,
    UserStatus status,
    UserRole role,
    AccountInfo account) {
  public record AccountInfo(
      Long id,
      String name,
      AccountType type,
      AccountStatus status,
      SubscriptionInfo subscription) {}

  public record SubscriptionInfo(
      Long id,
      SubscriptionPlan plan,
      SubscriptionStatus status,
      Instant startedAt,
      Instant expiresAt,
      Instant cancelledAt) {}
}
