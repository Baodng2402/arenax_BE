package com.bk.arenax.domain.user;

import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.common.BaseEntity;
import com.bk.arenax.domain.rbac.Role;
import com.bk.arenax.domain.rbac.UserRoleAssignment;
import jakarta.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity(name = "users")
public class User extends BaseEntity implements UserDetails {
  String name;
  String email;
  String password;

  @Column(name = "full_name", length = 120)
  String fullName;

  @Column(name = "display_name", length = 80)
  String displayName;

  @Column(name = "phone_number", length = 30)
  String phoneNumber;

  @Column(name = "avatar_url", length = 500)
  String avatarUrl;

  @Enumerated(EnumType.STRING)
  Gender gender;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  UserStatus status = UserStatus.ACTIVE;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  UserRole role = UserRole.USER;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  Account account;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  Set<UserRoleAssignment> roleAssignments = new HashSet<>();

  public Set<Role> roles() {
    return roleAssignments.stream().map(UserRoleAssignment::getRole).collect(Collectors.toSet());
  }

  public Set<Role> rolesForAccount(Long accountId) {
    return roleAssignments.stream()
        .filter(assignment -> sameAccount(assignment.getAccount(), accountId))
        .map(UserRoleAssignment::getRole)
        .collect(Collectors.toSet());
  }

  public void replaceRoles(Set<Role> roles) {
    replaceRolesForAccount(account, roles);
  }

  public void replaceRolesForAccount(Account account, Set<Role> roles) {
    Long accountId = account == null ? null : account.getId();
    roleAssignments.removeIf(assignment -> sameAccount(assignment.getAccount(), accountId));
    if (roles != null) {
      roles.forEach(role -> roleAssignments.add(new UserRoleAssignment(this, account, role)));
    }
  }

  public void assignRoles(Set<Role> roles) {
    replaceRoles(roles);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<String> authorities = new HashSet<>();
    roles()
        .forEach(
            role -> {
              authorities.add(role.authorityName());
              role.getPermissions()
                  .forEach(permission -> authorities.add(permission.getCodeName()));
            });
    return authorities.stream().map(SimpleGrantedAuthority::new).toList();
  }

  private boolean sameAccount(Account assignmentAccount, Long accountId) {
    Long assignmentAccountId = assignmentAccount == null ? null : assignmentAccount.getId();
    return Objects.equals(assignmentAccountId, accountId);
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return status != UserStatus.SUSPENDED;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return status == UserStatus.ACTIVE;
  }
}
