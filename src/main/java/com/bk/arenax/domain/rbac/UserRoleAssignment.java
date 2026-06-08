package com.bk.arenax.domain.rbac;

import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.common.BaseEntity;
import com.bk.arenax.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "user_role_assignments",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_user_role_assignments_user_account_role",
            columnNames = {"user_id", "account_id", "role_id"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRoleAssignment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  Account account;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", nullable = false)
  Role role;

  public UserRoleAssignment(User user, Account account, Role role) {
    this.user = user;
    this.account = account;
    this.role = role;
  }
}
